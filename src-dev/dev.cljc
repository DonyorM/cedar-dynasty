(ns dev
  (:require
    great-dalmuti.main
    [hyperfiddle.electric :as e]
    #?(:clj [great-dalmuti.server-jetty :as jetty])
    #?(:clj [shadow.cljs.devtools.api :as shadow])
    #?(:clj [shadow.cljs.devtools.server :as shadow-server])
    #?(:clj [clojure.tools.logging :as log])))

(comment (-main)) ; repl entrypoint

#?(:clj ;; Server Entrypoint
   (do
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

       (comment (.stop server))
       )))

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
