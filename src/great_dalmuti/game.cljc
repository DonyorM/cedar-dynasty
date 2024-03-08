(ns great-dalmuti.game
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.components.hand :refer [Hand]]
            [great-dalmuti.components.card :refer [Card]]
            [great-dalmuti.components.button :refer [Button]]))

(defn player-to-hand
  [player]
  {::spec/user-id (::spec/user-id player)
   ::spec/name (::spec/name player)
   ::spec/count (count (::spec/cards player))})

(e/defn Game [game]
  ;; game is a :spec/game
  (e/server
    (let [hands (map player-to-hand (::spec/players game))
          play (::spec/play game)]
      (e/client
        (dom/div (dom/props {:class "flex flex-col justify-between"})
                 (dom/div
                   (dom/props {:class "flex items-center w-full justify-center gap-4 flex-wrap"})
                   (e/for-by ::spec/user-id [hand hands]
                             (Hand. hand)))
                 (dom/div (dom/props {:class "flex justify-around"})
                          (dom/div (dom/props {:class "w-36 flex flex-col gap-8 justify-center"})
                            (Button. {:text "SKIP"})
                            (Button. {:text "PLAY"}))
                          (Card. (::spec/card play) (::spec/count play))))))))