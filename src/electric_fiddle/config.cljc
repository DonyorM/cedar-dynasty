(ns electric-fiddle.config
  #?(:cljs (:require-macros [electric-fiddle.config :refer [install-fiddle]]))
  (:require [hyperfiddle.electric :as e]))

#?(:clj (def app-version (System/getProperty "HYPERFIDDLE_ELECTRIC_SERVER_VERSION")))
#?(:clj (def datomic-conn))
#?(:clj (def electric-server-config 
          {:host "0.0.0.0", :port 8080, 
           :resources-path "public" 
           :manifest-path "public/js/manifest.edn"})) ; shadow output

#?(:clj (def ^:dynamic *hyperfiddle-user-ns* nil)) ; cljs comptime, see build.clj
(defmacro install-fiddles [] (symbol (name *hyperfiddle-user-ns*) "fiddles"))

(e/def pages (install-fiddles)) ; client