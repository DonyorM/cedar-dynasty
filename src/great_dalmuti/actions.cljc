(ns great-dalmuti.actions
  (:require [clojure.spec.alpha :as s]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.utils :as u]))
(defn ^:private spec-player-in-game [args] (some #{(-> args :play ::spec/user-id)}
                                                 (map ::spec/user-id (-> args :game ::spec/players))))
(defn play-valid
  "Checks if a given play is valid compared to an existing play"
  [existing-play new-play]
  (or (nil? existing-play)
      (and (= (::spec/count existing-play) (::spec/count new-play))
           (<= (.indexOf spec/card-order (::spec/card new-play)) (.indexOf spec/card-order (::spec/card existing-play))))))

(s/fdef play-valid
        :args  (s/cat :existing-play ::spec/play :new-play (s/nilable ::spec/play))
        :ret boolean?)

(defn play-valid-for-game
  [game play]
  (boolean (let [player (u/user-for-id game (::spec/user-id play))]
             (and player
                  (play-valid (::spec/play game) play)
                  (<= (::spec/count play)
                      (-> player
                          ::spec/cards
                          (get (::spec/card play) 0)))))))

(s/fdef play-valid-for-game
        :args (s/with-gen (s/and (s/cat :game ::spec/game :play ::spec/play)
                                 spec-player-in-game)
                          spec/player-in-game-gen)
        :ret boolean?)

(defn make-play
  "Takes a game and a given play and adjust the game to have that play made.

  If the play is invalid it returns the original game"
  [game play]
  (if-not (play-valid-for-game game play)
    game
    (let [player-index (u/player-index game (::spec/user-id play))
          next-play-index (if (= player-index (dec (count (::spec/players game))))
                            0
                            (inc player-index))]
      (-> game
          (update-in [::spec/players player-index ::spec/cards (::spec/card play)] #(- % (::spec/count play)))
          (assoc ::spec/play play
                 ::spec/current-player (get-in game [::spec/players next-play-index ::spec/user-id]))))))

(s/fdef make-play
        :args (s/with-gen (s/and (s/cat :game ::spec/game :play ::spec/play)
                                 spec-player-in-game)
                          spec/player-in-game-gen)
        :ret ::spec/game)
