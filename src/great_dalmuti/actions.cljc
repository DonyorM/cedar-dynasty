(ns great-dalmuti.actions
  (:require [clojure.spec.alpha :as s]
            [clojure.set :as sets]
            [clojure.test.check.generators :as gen]
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

(defn- find-next-player-index
  [players player-index]
  (->> (range (count players))
       (map #(mod (inc (+ player-index %)) (count players)))
       (filter #(> (u/card-count (get-in players [% ::spec/cards])) 0))
       first))

(defn- check-for-winner
  [game player-index]
  (if (= 0 (u/card-count (get-in game [::spec/players player-index ::spec/cards])))
    (let [user-id (get-in game [::spec/players player-index ::spec/user-id])]
      (update game ::spec/win-order conj user-id))
    game))

(defn- check-for-loser
  [game]
  (let [losers (->> game
                    ::spec/players
                    (filter #(> (u/card-count (::spec/cards %)) 0)))]
    (println losers)
    (if (= (count losers) 1)
      (update game ::spec/win-order conj (::spec/user-id (first losers)))
      game)))

(defn make-play
  "Takes a game and a given play and adjust the game to have that play made.

  If the play is invalid it returns the original game"
  [game play]
  (if-not (play-valid-for-game game play)
    game
    (let [player-index (u/player-index game (::spec/user-id play))
          next-play-index (find-next-player-index (::spec/players game) player-index)]
      (-> game
          (update-in [::spec/players player-index ::spec/cards (::spec/card play)] #(- % (::spec/count play)))
          (assoc ::spec/play play
                 ::spec/current-player (get-in game [::spec/players next-play-index ::spec/user-id]))
          (check-for-winner player-index)
          check-for-loser))))

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
(defn move-cards
  [game from-user-id card-map]
  (let [from-user (u/user-for-id game from-user-id)
        taxation (-> game ::spec/card-debts (get from-user-id))
        count-moved (apply + (vals card-map))]
    (if taxation
      (if (every? #(<= (% card-map) (-> from-user ::spec/cards %)) (keys card-map))
        (if (<= count-moved (::spec/count taxation))
          (let [to-user-id (::spec/to taxation)
                to-user-loc (u/player-index game to-user-id)
                from-user-loc (u/player-index game from-user-id)
                cards-adjusted (reduce (fn [updated-game card]
                                         (-> updated-game
                                             (update-in [::spec/players from-user-loc ::spec/cards card] - (get card-map card))
                                             (update-in [::spec/players to-user-loc ::spec/cards card] #(+ (or %1 0) %2) (get card-map card))))
                                       game
                                       (keys card-map))]
            (if (= count-moved (::spec/count taxation))
              (update cards-adjusted ::spec/card-debts dissoc from-user-id)
              (update-in cards-adjusted [::spec/card-debts from-user-id ::spec/count] - count-moved)))
          (u/set-errors game (str "That's too many cards. You only owe " (::spec/count taxation))))
        (u/set-errors game "You don't have enough cards to give that"))
      (u/set-errors game "You don't owe any cards"))))

(s/fdef move-cards
        :args (s/cat :game ::spec/game :from-user-id ::spec/user-id :card-map (s/map-of ::spec/card (s/and pos-int?
                                                                                                           #(< % (/
                                                                                                                   #?(:clj Long/MAX_VALUE)
                                                                                                                   #?(:cljs (. js/Number -MAX_SAFE_INTEGER))
                                                                                                                   (count spec/card-order))))))
        :ret ::spec/game)

(defn ^:private get-best-card
  [player]
  (first (filter #(> (get (::spec/cards player) % 0) 0) spec/card-order)))
(defn tax-player
  [game from-id to-id count]
  (if (= from-id to-id)
    (u/set-errors game "Can't move cards from user to themselves")
    (let [from-player (u/user-for-id game from-id)
          from-player-index (u/player-index game from-id)
          to-player-index (u/player-index game to-id)
          best-card (get-best-card from-player)
          best-card-count (get (::spec/cards from-player) best-card 0)
          taxed-count (min count best-card-count)]
      (if best-card
        (let [result (-> game
                         (update-in [::spec/players from-player-index ::spec/cards best-card] - taxed-count)
                         (update-in [::spec/players to-player-index ::spec/cards best-card] #(+ (or % 0) taxed-count)))
              final-result (if (= count taxed-count)
                             result
                             (tax-player (u/set-errors result nil) from-id to-id (- count taxed-count)))]
          (if (u/get-errors final-result)
            (u/set-errors game (u/get-errors final-result))
            final-result))
        (u/set-errors game (str "User doesn't have enough cards to tax"))))))

(s/fdef tax-player
        :args (s/and (s/cat :game ::spec/game :from-id ::spec/user-id :to-id ::spec/user-id :count (s/with-gen pos-int?
                                                                                                               #(gen/choose 1 2))) ; In a real game you only move one or two cards
                     (fn [{:keys [game from-id]}]
                       (some #{from-id} (map ::spec/user-id (::spec/players game))))
                     (fn [{:keys [game to-id]}]
                       (some #{to-id} (map ::spec/user-id (::spec/players game)))))
        :ret ::spec/game
        :fn (s/or :error #(u/get-errors (:ret %))
                  :success (s/and (fn [{:keys [args ret]}]
                                    (= (+ (u/card-count (::spec/cards (u/user-for-id (:game args) (:to-id args))))
                                          (:count args))
                                       (u/card-count (::spec/cards (u/user-for-id ret (:to-id args))))))
                                  (fn [{:keys [args ret]}]
                                    (= (- (u/card-count (::spec/cards (u/user-for-id (:game args) (:from-id args))))
                                          (:count args))
                                       (u/card-count (::spec/cards (u/user-for-id ret (:from-id args)))))))))

(def GREAT_GIVE_COUNT 2)
(def LESSER_GIVE_COUNT 1)
(defn start-new-round
  [game]
  (let [win-order (::spec/win-order game)]

    (if (or (not= (count win-order) (count (::spec/players game)))
            (some #(not (some #{%} win-order)) (map ::spec/user-id (::spec/players game))))
      (u/set-errors game "Win order not specified")
      (-> game
          (assoc ::spec/players (sort-by
                                  #(.indexOf win-order (::spec/user-id %))
                                  (::spec/players game))
                 ::spec/card-debts {(first win-order)  {::spec/to (last win-order) ::spec/count GREAT_GIVE_COUNT},
                                    (second win-order) {::spec/to (nth win-order (- (count win-order) 2)) ::spec/count LESSER_GIVE_COUNT}}
                 ::spec/win-order []
                 ::spec/current-player (first win-order))
          deal-cards
          (tax-player (last win-order) (first win-order) GREAT_GIVE_COUNT)
          (tax-player (nth win-order (- (count win-order) 2)) (second win-order) LESSER_GIVE_COUNT)))))

(defn ^:private check-best-card
  [winner loser]
  (let [winner-card (get-best-card winner)
        loser-card (get-best-card loser)]
    (and winner-card
         (or (not loser-card)
             (<= (.indexOf spec/card-order winner-card)
                 (.indexOf spec/card-order loser-card))))))
(s/fdef start-new-round
        :args (s/cat :game ::spec/game)
        :ret ::spec/game
        :fn (s/or :error #(u/get-errors (:ret %))
                  :success (s/and (fn [{:keys [args ret]}]
                                    (every? identity (map #(= %1 (::spec/user-id %2))
                                                          (-> args :game ::spec/win-order)
                                                          (::spec/win-order ret))))
                                  (fn [{:keys [ret]}]
                                    (check-best-card (-> ret ::spec/players first) (-> ret ::spec/players last)))
                                  (fn [{:keys [ret]}]
                                    (check-best-card (-> ret ::spec/players second) (-> ret ::spec/players (#(nth % (- (count %) 2))))))
                                  #(= (count (-> % :ret ::spec/card-debts)) 2))))

(defn skip
  [game]
  (let [current-user-index (u/player-index game (::spec/current-player game))
        next-player-id (get-in game [::spec/players (find-next-player-index (::spec/players game) current-user-index) ::spec/user-id])
        result-game (assoc game ::spec/current-player next-player-id)]
    (if (and next-player-id
             (not= next-player-id (::spec/current-player game)))
      (let [play-user-id (get-in result-game [::spec/play ::spec/user-id])]
        (if (or (= play-user-id next-player-id)
                (some #{play-user-id} (::spec/win-order result-game)))
          (assoc result-game ::spec/play nil)
          result-game))
      (u/set-errors game "No valid player to skip to"))))

(s/fdef skip
        :args (s/cat :game ::spec/game)
        :ret ::spec/game
        :fn (s/or :success #(not= (-> % :args :game ::spec/current-player) (-> % :ret ::spec/current-player))
                  :error #(u/get-errors (:ret %))))