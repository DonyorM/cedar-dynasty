(ns great-dalmuti.actions
  (:require [clojure.spec.alpha :as s]
            [clojure.set :as sets]
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
        :args (s/cat :existing-play ::spec/play :new-play (s/nilable ::spec/play))
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

(defn upsert-player
  [game player-id player-name]
  (if-let [player-index (u/player-index game player-id)]
    (assoc-in game [::spec/players player-index ::spec/name] player-name)
    (update game ::spec/players conj {::spec/user-id player-id ::spec/name player-name ::spec/cards {}})))

(s/fdef upsert-player
        :args (s/cat :game ::spec/game :player-id ::spec/user-id :player-name ::spec/name)
        :ret ::spec/game
        :fn (fn [{:keys [args ret]}]
              (some
                #{[(:player-id args) (:player-name args)]}
                (map
                  (fn [p] [(::spec/user-id p) (::spec/name p)])
                  (::spec/players ret)))))

(def DEFAULT_DECK (vec (apply concat
                      (map #(repeat % (keyword (str %))) (range 1 13)))))
(defn deal-cards
  ([game]
   (deal-cards game (shuffle DEFAULT_DECK)))
  ([game shuffled-deck]
   (if (= 0 (count (::spec/players game)))
     game
     (let [card-num (quot (count shuffled-deck) (count (::spec/players game)))]
       (if (= card-num 0)
         (do (println "Not enough cards to deal for this many players!")
             game)
         (assoc (->> (::spec/players game)
                     (mapv #(assoc %2 ::spec/cards (frequencies %1))
                           (partition card-num shuffled-deck))
                     (assoc game ::spec/players))
           ::spec/play
           nil))))))

(s/fdef deal-cards
        :args (s/and (s/cat :game ::spec/game :shuffled-deck (s/coll-of ::spec/card))
                     #(>= (count (:shuffled-deck %)) (count (-> % :game ::spec/players))))
        :ret ::spec/game
        :fn (s/and
              (fn [{:keys [args ret]}]
                (let [args-freq (frequencies (:shuffled-deck args))
                      ret-freq (frequencies (apply concat (map ::spec/cards (::spec/players ret))))]
                  (every?
                    #(<= (get % ret-freq 0) (get % args-freq 0))
                    (sets/union (set (keys args-freq)) (set (keys ret-freq))))))
              (fn [{:keys [ret]}]
                (apply = (map #(apply + (vals (::spec/cards %))) (::spec/players ret))))
              (fn [{:keys [args ret]}]
                (< (- (count (:shuffled-deck args))
                      (apply +
                             (map #(apply + (vals (::spec/cards %)))
                                  (::spec/players ret))))
                   (count (::spec/players ret))))
              #(= (count (-> % :args :game ::spec/players)) (count (-> % :ret ::spec/players)))
              #(nil? (-> % :ret ::spec/play))))