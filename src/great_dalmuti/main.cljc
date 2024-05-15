(ns great-dalmuti.main
  (:require [great-dalmuti.actions :as a]
            [great-dalmuti.utils :as u]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.actions :as a]
            [great-dalmuti.game :refer [Game]]
            [great-dalmuti.login :refer [Login]]
            [great-dalmuti.components.button :refer [Button]]))

;; Saving this file will automatically recompile and update in your browser

(def bob-id (random-uuid))
(def !game
  (atom {::spec/players [{::spec/user-id bob-id
                          ::spec/name    "Bob Jones"
                          ::spec/cards   {:1 1, :2 3, :3 2, :4 1}}
                         {::spec/user-id (random-uuid)
                          ::spec/name    "Jane Eyre"
                          ::spec/cards   {:1 1, :2 1, :3 2, :4 2, :5 2}}
                         {::spec/user-id (random-uuid)
                          ::spec/name    "Jerry Foster"
                          ::spec/cards   {:1 1, :2 1, :3 2, :4 1, :6 3, :12 4}}]
         ::spec/current-player bob-id
         ::spec/play    nil #_{::user-id bob-id ::spec/count 2 ::spec/card :3}})
  )

(defn new-game [current-player-id current-player-name]
  {::spec/players [{::spec/user-id current-player-id
                    ::spec/name current-player-name
                    ::spec/cards {}}]
   ::spec/play nil
   ::spec/current-player current-player-id})

(e/defn Main [ring-request]
  (e/server
    (let [!current-player (atom bob-id)
          current-player-id (e/watch !current-player)
          game (e/watch !game)]
      (e/client
        (binding [dom/node js/document.body]
          (dom/div (dom/props {:class "min-h-screen bg-sky-800 text-white h-screen"})
                   (dom/div (dom/props {:class "flex justify-center gap-6 w-full"})
                            (Button. {:text "Re-deal"
                                      :on-click (e/fn []
                                                  (e/server (swap! !game a/deal-cards)))})
                            (Button. {:text "New Game"
                                      :on-click (e/fn []
                                                  (e/server (reset! !game (new-game current-player-id (::spec/name (u/user-for-id game current-player-id))))))}))
                   (if (and current-player-id (some #{current-player-id} (map ::spec/user-id (::spec/players game))))
                     (e/server
                       (Game. !game current-player-id))
                     (Login.
                       (e/fn [user-id name]
                         (e/server
                           (swap! !game a/upsert-player user-id name)
                           (reset! !current-player user-id)))))))))))