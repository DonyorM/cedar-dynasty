(ns cedar-dynasty.main
  (:require [contrib.electric-goog-history :as history]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [cedar-dynasty.utils.routes :as routes]
            [cedar-dynasty.scenes.game :refer [Game]]
            [cedar-dynasty.scenes.login :refer [Login]]
            [cedar-dynasty.scenes.new-game :refer [NewGame]]
            [cedar-dynasty.components.button :refer [Button]]))

(e/defn Main [ring-request]
  (e/server
    (let [current-player-id (get-in ring-request [:session :user-id])]
      (e/client
        (binding [dom/node js/document.body
                  routes/route-match routes/re-router
                  routes/route-name (some-> routes/re-router :data :name)]
          (let [route history/path]
            (dom/div (dom/props {:class "min-h-screen bg-sky-800 text-white h-screen"})
                     (if current-player-id
                       (if-let [code (some-> routes/route-match :path-params :code)]
                         (e/server
                           (Game. code current-player-id))
                         (NewGame.))
                       (Login.)))))))))