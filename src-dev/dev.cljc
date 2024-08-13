(ns dev
  (:require
    great-dalmuti.main
    [hyperfiddle.electric :as e]
    #?(:clj [great-dalmuti.server-jetty :as jetty])
    #?(:clj [shadow.cljs.devtools.api :as shadow])
    #?(:clj [shadow.cljs.devtools.server :as shadow-server])
    #?(:clj [clojure.tools.logging :as log])
    #?(:clj [nrepl.server :as nrepl])))


(comment (-main)) ; repl entrypoint

#?(:clj ;; Server Entrypoint
   (do
     (def NREPL_PORT (or (System/getenv "NREPL_PORT") 9043))
     (def NREPL_HOST (or (System/getenv "NREPL_HOST") "0.0.0.0"))
     (log/info (str "Starting nrepl server on port " NREPL_HOST ":" NREPL_PORT "..."))
     (defonce nrepl-server (nrepl/start-server :port NREPL_PORT :bind NREPL_HOST))
     (def config
       {:host "0.0.0.0"
        :port 8090
        :source-paths ["src"]
        :resources-path "public/great_dalmuti"
        :manifest-path ; contains Electric compiled program's version so client and server stays in sync
        "public//great_dalmuti/js/manifest.edn"})

     (defn -main [& args]
       (log/info "Starting Electric compiler and server...")

       (shadow-server/start!)
       (shadow/watch :dev)
       (comment (shadow-server/stop!))

       (def server (jetty/start-server!
                     (fn [ring-request]
                       (e/boot-server {} great-dalmuti.main/Main ring-request))
                     config))

       (comment (.stop server)))))

#?(:cljs ;; Client Entrypoint
   (do
     (def electric-entrypoint (e/boot-client {} great-dalmuti.main/Main nil))

     (defonce reactor nil)

     (defn ^:dev/after-load ^:export start! []
       (set! reactor (electric-entrypoint
                       #(js/console.log "Reactor success:" %)
                       #(js/console.error "Reactor failure:" %))))

     (defn ^:dev/before-load stop! []
       (when reactor (reactor)) ; stop the reactor
       (set! reactor nil))))
