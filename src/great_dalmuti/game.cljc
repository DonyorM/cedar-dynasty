(ns great-dalmuti.game
  (:require [clojure.spec.alpha :as s]
            [great-dalmuti.utils :as u]
            [great-dalmuti.actions :as a]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.components.hand :refer [Hand]]
            [great-dalmuti.components.card :refer [Card]]
            [great-dalmuti.components.button :refer [Button]]
            [great-dalmuti.components.hand-wheel :refer [HandWheel]]
            [great-dalmuti.components.count-select :refer [CountSelect]]))

(defn player-to-hand
  [player]
  {::spec/user-id (::spec/user-id player)
   ::spec/name    (::spec/name player)
   ::spec/count   (apply + (vals (::spec/cards player)))})
(s/fdef player-to-hand
        :args (s/cat :players ::spec/player)
        :ret ::spec/hand)

(defn current-play
  [game current-player-id selected-card selected-count]
  (let [current-play-count (get-in game [::spec/play ::spec/count])]
    (when selected-card
      {::spec/user-id current-player-id
       ::spec/card    selected-card
       ::spec/count   (if current-play-count
                        current-play-count
                        selected-count)})))

(e/defn Game [!game current-player-id]
  ;; game is a :spec/game
  (e/server
    (let [game (e/watch !game)
          hands (map player-to-hand (::spec/players game))
          play (::spec/play game)
          player-hand (u/user-for-id game current-player-id)
          game-over (= (count (::spec/players game)) (count (::spec/win-order game)))]
      (e/client
        (let [!selected-card (atom nil)
              selected-card (e/watch !selected-card)
              !selected-count (atom nil)
              selected-count (e/watch !selected-count)
              is-current-player (= current-player-id (::spec/current-player game))]
          (dom/div (dom/props {:class "flex flex-col justify-between min-h-screen gap-4"})
                   (dom/div
                     (dom/props {:class "flex items-center w-full justify-center gap-4 flex-wrap"})
                     (e/for-by ::spec/user-id [hand hands]
                               (Hand. hand)))
                   (if game-over
                     (dom/div (dom/props {:class "flex justify-around"})
                              (dom/text "GAME OVER"))
                     (dom/div (dom/props {:class "flex justify-around"})
                              (dom/div (dom/props {:class "w-36 flex flex-col gap-8"})
                                       (Button. {:text     "SKIP"
                                                 :disabled (not is-current-player)
                                                 :on-click (e/fn []
                                                             (e/server
                                                               (swap!
                                                                 !game
                                                                 a/skip)))})
                                       (Button. {:text     "PLAY"
                                                 :disabled (not (and is-current-player
                                                                     (or play selected-count)
                                                                     (a/play-valid-for-game
                                                                       game
                                                                       (current-play game current-player-id selected-card selected-count))))
                                                 :on-click (e/fn []
                                                             (e/server
                                                               (swap!
                                                                 !game
                                                                 #(a/make-play
                                                                    %
                                                                    (current-play % current-player-id selected-card selected-count)))))}))
                              (Card. (::spec/card play) (::spec/count play) {})))
                   (dom/div (dom/props {:class "flex-grow"}))
                   (when player-hand
                     (when (and selected-card (nil? (::spec/play game)))
                       (CountSelect.
                         selected-count
                         #(reset! !selected-count %)
                         (get-in player-hand [::spec/cards selected-card])))
                     (HandWheel. (::spec/cards player-hand)
                                 {:selected  selected-card
                                  :on-select (fn [card]
                                               (reset! !selected-count nil)
                                               (reset! !selected-card card))}))))))))