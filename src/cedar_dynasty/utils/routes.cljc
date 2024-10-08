(ns cedar-dynasty.utils.routes
  (:require
    [hyperfiddle.electric :as e]
    [missionary.core :as m]
    [reitit.core :as r]
    #?(:cljs [reitit.frontend.easy :as rfe])))

(e/def route-match)
(e/def route-name)



(def router
  (r/router
    [["/game/:code" :game]]))

#?(:cljs (e/def re-router
           (->> (m/observe
                  (fn [!]
                    (rfe/start!
                      router
                      !
                      {:use-fragment false})))
                (m/relieve {})
                new)))

(defn encode-uri [_router]
  (fn [{:keys [path]}]
    path))

(defn decode-uri [router]
  (fn [uri-str]
    (r/match-by-path router uri-str)))

(e/defn Navigate
  [to params]
  (e/client (rfe/navigate to params)))
