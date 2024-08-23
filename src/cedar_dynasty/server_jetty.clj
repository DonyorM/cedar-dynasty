(ns cedar-dynasty.server-jetty
  "Electric integrated into a sample ring + jetty app."
  (:require
    [cedar-dynasty.backend.db :as db]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [contrib.assert :refer [check]]
    [hyperfiddle.electric-ring-adapter :as electric-ring]
    [ring.adapter.jetty :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.cookies :as cookies]
    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.resource :refer [wrap-resource]]
    [ring.util.response :as res]
    [ring.middleware.oauth2 :refer [wrap-oauth2]]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [ring.middleware.session.memory]
    [cedar-dynasty.backend.config :as config]
    [cedar-dynasty.backend.auth :as auth])
  (:import
    (org.eclipse.jetty.server.handler.gzip GzipHandler)
    (org.eclipse.jetty.websocket.server.config JettyWebSocketServletContainerInitializer JettyWebSocketServletContainerInitializer$Configurator)))

;;; Electric integration

(defn electric-websocket-middleware
  "Open a websocket and boot an Electric server program defined by `entrypoint`.
  Takes:
  - a ring handler `next-handler` to call if the request is not a websocket upgrade (e.g. the next middleware in the chain),
  - a `config` map eventually containing {:hyperfiddle.electric/user-version <version>} to ensure client and server share the same version,
    - see `hyperfiddle.electric-ring-adapter/wrap-reject-stale-client`
  - an Electric `entrypoint`: a function (fn [ring-request] (e/boot-server {} my-ns/My-e-defn ring-request))
  "
  [next-handler config entrypoint]
  ;; Applied bottom-up
  (-> (electric-ring/wrap-electric-websocket next-handler entrypoint) ; 5. connect electric client
      ; 4. this is where you would add authentication middleware (after cookie parsing, before Electric starts)
      (cookies/wrap-cookies)                                ; 3. makes cookies available to Electric app
      (electric-ring/wrap-reject-stale-client config)       ; 2. reject stale electric client
      (wrap-params)))                                       ; 1. parse query params

(defn get-modules [manifest-path]
  (when-let [manifest (io/resource manifest-path)]
    (let [manifest-folder (when-let [folder-name (second (rseq (str/split manifest-path #"\/")))]
                            (str "/" folder-name "/"))]
      (->> (slurp manifest)
           (edn/read-string)
           (reduce (fn [r module] (assoc r (keyword "hyperfiddle.client.module" (name (:name module)))
                                           (str manifest-folder (:output-name module)))) {})))))

(defn template
  "In string template `<div>$:foo/bar$</div>`, replace all instances of $key$
with target specified by map `m`. Target values are coerced to string with `str`.
  E.g. (template \"<div>$:foo$</div>\" {:foo 1}) => \"<div>1</div>\" - 1 is coerced to string."
  [t m] (reduce-kv (fn [acc k v] (str/replace acc (str "$" k "$") (str v))) t m))

;;; Template and serve index.html

(defn wrap-index-page
  "Server the `index.html` file with injected javascript modules from `manifest.edn`.
`manifest.edn` is generated by the client build and contains javascript modules
information."
  [next-handler config]
  (fn [ring-req]
    (if-let [response (res/resource-response (str (check string? (:resources-path config)) "/index.html"))]
      (-> (if-let [bag (merge config (get-modules (check string? (:manifest-path config))))]
            (-> (res/response (template (slurp (:body response)) bag)) ; TODO cache in prod mode
                (res/content-type "text/html")              ; ensure `index.html` is not cached
                (res/header "Cache-Control" "no-store")
                (res/header "Last-Modified" (get-in response [:headers "Last-Modified"])))
            (-> (res/not-found (pr-str ::missing-shadow-build-manifest)) ; can't inject js modules
                (res/content-type "text/plain")))
          (assoc :session (:session ring-req)))
      ;; index.html file not found on classpath
      (next-handler ring-req))))

(defn not-found-handler [_ring-request]
  (-> (res/not-found "Not found")
      (res/content-type "text/plain")))

(defn handle-sign-in
  [request]
  (let [id-token (get-in request [:oauth2/access-tokens :cognito :id-token])]
    {:status  302
     :headers {"Location" "/"}
     :session (assoc (:session request) :user-id (auth/get-user-id id-token))}))

(defn custom-routes [next-handler]
  (fn [{:keys [uri request-method] :as request}]
    (let [signature [uri request-method]]
      (cond
        (= signature ["/oauth2-landing" :get]) (handle-sign-in request)
        (= signature ["/health" :get]) {:status 200}
        :default (next-handler request)))))

(defn http-middleware [config]
  ;; these compose as functions, so are applied bottom up
  (-> not-found-handler
      (wrap-index-page config)                              ; 3. otherwise fallback to default page file
      custom-routes
      (wrap-oauth2 {:cognito
                    {:authorize-uri    (str config/COGNITO_UI_URL "/oauth2/authorize")
                     :access-token-uri (str config/COGNITO_UI_URL "/oauth2/token")
                     :client-id        config/COGNITO_CLIENT_ID
                     :client-secret    config/COGNITO_CLIENT_SECRET
                     :scopes           ["openid", "email"]
                     :launch-uri       "/oauth2"
                     :redirect-uri     "/oauth2-return"
                     :landing-uri      "/oauth2-landing"
                     }})
      (wrap-params)
      (wrap-resource (:resources-path config))              ; 2. serve static file from classpath
      (wrap-content-type)                                   ; 1. detect content (e.g. for index.html)
      ))



(defn middleware [config entrypoint]
  (let [session-store (ring.middleware.session.memory/memory-store)]
    (-> (http-middleware config)                            ; 2. otherwise, serve regular http content
        (electric-websocket-middleware config entrypoint)   ; 1. intercept websocket upgrades and maybe start Electric

        (wrap-defaults (-> site-defaults (assoc-in [:session :cookie-attrs :same-site] :lax)
                           (assoc-in [:session :store] session-store))))))

(defn- add-gzip-handler!
  "Makes Jetty server compress responses. Optional but recommended."
  [server]
  (.setHandler server
               (doto (GzipHandler.)
                 #_(.setIncludedMimeTypes (into-array ["text/css" "text/plain" "text/javascript" "application/javascript" "application/json" "image/svg+xml"])) ; only compress these
                 (.setMinGzipSize 1024)
                 (.setHandler (.getHandler server)))))

(defn- configure-websocket!
  "Tune Jetty Websocket config for Electric compat." [server]
  (JettyWebSocketServletContainerInitializer/configure
    (.getHandler server)
    (reify JettyWebSocketServletContainerInitializer$Configurator
      (accept [_this _servletContext wsContainer]
        (.setIdleTimeout wsContainer (java.time.Duration/ofSeconds 60))
        (.setMaxBinaryMessageSize wsContainer (* 100 1024 1024)) ; 100M - temporary
        (.setMaxTextMessageSize wsContainer (* 100 1024 1024)) ; 100M - temporary
        ))))

(defn start-server! [entrypoint
                     {:keys [port host]
                      :or   {port 8080, host "0.0.0.0"}
                      :as   config}]
  (db/check-database-setup!)
  (let [server (ring/run-jetty (middleware config entrypoint)
                               (merge {:port         port
                                       :join?        false
                                       :configurator (fn [server]
                                                       (configure-websocket! server)
                                                       (add-gzip-handler! server))}
                                      config))]
    (log/info "👉" (str "http://" host ":" (-> server (.getConnectors) first (.getPort))))
    server))
