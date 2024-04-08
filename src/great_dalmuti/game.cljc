(ns great-dalmuti.game
  (:require [clojure.spec.alpha :as s]
            [great-dalmuti.utils :as u]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.components.hand :refer [Hand]]
            [great-dalmuti.components.card :refer [Card]]
            [great-dalmuti.components.button :refer [Button]]
            [great-dalmuti.components.hand-wheel :refer [HandWheel]]))

(defn player-to-hand
  [player]
  {::spec/user-id (::spec/user-id player)
   ::spec/name    (::spec/name player)
   ::spec/count   (apply + (vals (::spec/cards player)))})
(s/fdef player-to-hand
        :args (s/cat :players ::spec/player)
        :ret ::spec/hand)

(e/defn Game [game current-player-id]
  ;; game is a :spec/game
  (e/server
    (let [hands (map player-to-hand (::spec/players game))
          play (::spec/play game)
          player-hand (u/user-for-id game current-player-id)]
      (println "hand:" player-hand)
      (e/client
        (let [!selected-card (atom nil)
              selected-card (e/watch !selected-card)]
          (dom/div (dom/props {:class "flex flex-col justify-between min-h-screen gap-4"})
                   (dom/div
                     (dom/props {:class "flex items-center w-full justify-center gap-4 flex-wrap"})
                     (e/for-by ::spec/user-id [hand hands]
                               (Hand. hand)))
                   (dom/div (dom/props {:class "flex justify-around"})
                            (dom/div (dom/props {:class "w-36 flex flex-col gap-8"})
                                     (Button. {:text "SKIP"})
                                     (Button. {:text "PLAY"}))
                            (Card. (::spec/card play) (::spec/count play) {:selected "test"}))
                   (dom/div (dom/props {:class "flex-grow"}))
                   (when player-hand
                     (HandWheel. (::spec/cards player-hand)
                                 {:selected  selected-card
                                  :on-select (fn [card]
                                               (println "ok?")
                                               #_(reset! !selected-card card))}))))))))