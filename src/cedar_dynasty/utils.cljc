(ns cedar-dynasty.utils
  (:require [clojure.spec.alpha :as s]
            [cedar-dynasty.spec :as spec]))

(defn user-for-id
  "Finds a user for the given id in a game"
  [game user-id]
  (->> game
      ::spec/players
      (filter #(= (::spec/user-id %) user-id))
       first))

(s/fdef user-for-id
        :args (s/cat :game ::spec/game :user-id ::spec/user-id)
        :ret (s/nilable ::spec/player)
        :fn (s/or :nil #(nil? (:ret %))
                  :user-id #(= (::spec/user-id (:ret %)) (-> % :args :user-id))))

(defn player-index
  [game user-id]
  (first (keep-indexed
           #(when (= (::spec/user-id %2) user-id) %1)
           (::spec/players game))))

(defn card-count
  [cards]
  (apply + (vals cards)))

(s/fdef card-count
        :args (s/cat :cards ::spec/cards)
        :ret nat-int?)

(defn set-errors
  [game error]
  (vary-meta game merge {:error error}))

(defn get-errors
  [game]
  (:error (meta game)))