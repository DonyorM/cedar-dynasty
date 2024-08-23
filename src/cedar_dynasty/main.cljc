(ns cedar-dynasty.main
  (:require [contrib.electric-goog-history :as history]
            [cedar-dynasty.actions :as a]
            [cedar-dynasty.utils :as u]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [cedar-dynasty.spec :as spec]
            [cedar-dynasty.actions :as a]
            #?(:clj [cedar-dynasty.backend.db :as db])
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
          #_(history/DemoGoogHistory.)
          (let [route history/path]
            (dom/div (dom/props {:class "min-h-screen bg-sky-800 text-white h-screen"})
                     (if current-player-id
                       (if-let [code (some-> routes/route-match :path-params :code)]
                         (e/server
                           (Game. code current-player-id))
                         (NewGame.))
                       (Login.)))))))))