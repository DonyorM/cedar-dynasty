(ns great-dalmuti.main
  (:require [contrib.electric-goog-history :as history]
            [great-dalmuti.actions :as a]
            [great-dalmuti.utils :as u]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.actions :as a]
            #?(:clj [great-dalmuti.backend.db :as db])
            [great-dalmuti.utils.routes :as routes]
            [great-dalmuti.scenes.game :refer [Game]]
            [great-dalmuti.scenes.login :refer [Login]]
            [great-dalmuti.scenes.new-game :refer [NewGame]]
            [great-dalmuti.components.button :refer [Button]]))

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