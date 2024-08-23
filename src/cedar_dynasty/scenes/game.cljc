(ns cedar-dynasty.scenes.game
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            #?(:clj [cedar-dynasty.backend.db :as db])
            #?(:clj [clojure.core.async :refer [go]])
            [cedar-dynasty.utils :as u]
            [cedar-dynasty.actions :as a]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [cedar-dynasty.spec :as spec]
            [cedar-dynasty.components.hand :refer [Hand]]
            [cedar-dynasty.components.card :refer [Card]]
            [cedar-dynasty.components.button :refer [Button]]
            [cedar-dynasty.components.hand-wheel :refer [HandWheel]]
            [cedar-dynasty.components.count-select :refer [CountSelect]]
            [cedar-dynasty.scenes.collect-user-name :refer [CollectUserName]]))

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

(defn new-game [current-player-id current-player-name]
  (println "name is" current-player-name)
  {::spec/players [{::spec/user-id current-player-id
                    ::spec/name current-player-name
                    ::spec/cards {}}]
   ::spec/play nil
   ::spec/current-player current-player-id
   ::spec/win-order []})

(e/defn Game [game-code current-player-id]
  ;; game is a :spec/game
  (e/server
    (let [all-games (e/watch db/!current-games)
          game (get all-games game-code)
          !player-name (atom (:name (db/get-user current-player-id)))
          player-name (e/watch !player-name)]
      (if-not player-name
        (CollectUserName. current-player-id #(reset! !player-name %))
        (if (not game)
          (do
            (let [game-result (db/get-game game-code)]      ; get-game will automatically populate the !current-games atom
              (when-not game-result
                (db/reset-game! game-code (new-game current-player-id player-name))))
            (e/client
              (dom/p (dom/text "Loading..."))))
          (let [hands (map player-to-hand (::spec/players game))
                play (::spec/play game)
                player-hand (u/user-for-id game current-player-id)
                game-over (= (count (::spec/players game)) (count (::spec/win-order game)))
                required-taxes (seq (filter #(> (::spec/count %) 0) (vals (::spec/card-debts game))))]
            (if-not player-hand
              (do

                (db/swap-game! game-code update ::spec/players conj {::spec/user-id current-player-id
                                                                     ::spec/name    player-name
                                                                     ::spec/cards   {}})
                (e/client
                  (dom/p (dom/text "Loading..."))))
              (e/client
                (let [!selected-card (atom nil)
                      selected-card (e/watch !selected-card)
                      !selected-count (atom nil)
                      selected-count (e/watch !selected-count)
                      is-current-player (= current-player-id (::spec/current-player game))]
                  (dom/div (dom/props {:class "flex justify-center gap-6 w-full"})
                           (Button. {:text     "Re-deal"
                                     :on-click (e/fn []
                                                 (e/server (db/swap-game! game-code a/deal-cards [:1 :2 :2 :3 :3 :3 :4 :4])))})
                           (Button. {:text     "New Game"
                                     :on-click (e/fn []
                                                 (e/server (db/reset-game! game-code (new-game current-player-id player-name))))}))
                  (dom/div (dom/props {:class "flex flex-col justify-between min-h-screen gap-4"})
                           (when-let [errors (u/get-errors game)]
                             (dom/div (dom/text errors)))
                           (dom/div
                             (dom/props {:class "flex items-center w-full justify-center gap-4 flex-wrap"})
                             (e/for-by ::spec/user-id [hand hands]
                                       (Hand. hand (= (::spec/user-id hand) (::spec/current-player game)))))
                           (cond
                             game-over (dom/div (dom/props {:class "flex justify-around flex-col items-center gap-4"})
                                                (dom/p (dom/text "GAME OVER"))
                                                (Button.
                                                  {:text     "New Round"
                                                   :on-click (e/fn []
                                                               (e/server
                                                                 (db/swap-game! game-code a/start-new-round)))}))
                             required-taxes (if-let [user-debt (get (::spec/card-debts game) current-player-id)]
                                              (dom/div (dom/props {:class "flex justify-around flex-col items-center gap-4"})
                                                       (dom/p (dom/text "Give " (::spec/count user-debt) " card(s)"))
                                                       (Button.
                                                         {:text     "GIVE CARD"
                                                          :disabled (or (not selected-card)
                                                                        (= 0 (get-in player-hand [::spec/cards selected-card] 0)))
                                                          :on-click (e/fn []
                                                                      (e/server
                                                                        (db/swap-game! game-code
                                                                                       a/move-cards
                                                                                       current-player-id
                                                                                       {selected-card 1})))}))
                                              (dom/div (dom/props {:class "flex justify-around"})
                                                       (dom/text "Waiting for "
                                                                 (string/join ", " (map (comp ::spec/name (partial u/user-for-id game))
                                                                                        (keys (::spec/card-debts game))))
                                                                 " to give cards")))
                             :default (dom/div (dom/props {:class "flex justify-around "})
                                               (dom/div (dom/props {:class "w-36 flex flex-col gap-8"})
                                                        (Button. {:text     "SKIP"
                                                                  :disabled (not is-current-player)
                                                                  :on-click (e/fn []
                                                                              (e/server
                                                                                (db/swap-game!
                                                                                  game-code
                                                                                  a/skip)))})
                                                        (Button. {:text     "PLAY"
                                                                  :disabled (not (and is-current-player
                                                                                      (or play selected-count)
                                                                                      (a/play-valid-for-game
                                                                                        game
                                                                                        (current-play game current-player-id selected-card selected-count))))
                                                                  :on-click (e/fn []
                                                                              (e/server
                                                                                (db/swap-game!
                                                                                  game-code
                                                                                  #(a/make-play
                                                                                     %
                                                                                     (current-play % current-player-id selected-card selected-count)))))}))
                                               (Card. (::spec/card play) (::spec/count play) {})))
                           (dom/div (dom/props {:class "flex-grow"}))
                           (when player-hand
                             (when (and selected-card (nil? (::spec/play game)) (not required-taxes))
                               (CountSelect.
                                 selected-count
                                 #(reset! !selected-count %)
                                 (get-in player-hand [::spec/cards selected-card])))
                             (HandWheel. (::spec/cards player-hand)
                                         {:selected  selected-card
                                          :on-select (fn [card]
                                                       (reset! !selected-count nil)
                                                       (reset! !selected-card card))}))))))))))))