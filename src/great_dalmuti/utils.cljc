(ns great-dalmuti.utils
  (:require [clojure.spec.alpha :as s]
            [great-dalmuti.spec :as spec]))

(defn user-for-id
  "Finds a user for the given id in a game"
  [game user-id]
  (->> game
      ::spec/players
      (some #(= (::spec/user-id %) user-id))))

(s/fdef user-for-id
        :args (s/cat :game ::spec/game :user-id ::spec/user-id)
        :ret (s/nilable ::spec/player)
        :fn (s/or :nil #(nil? (:ret %))
                  :user-id #(= (::spec/user-id (:ret %)) (-> % :args :user-id))))
