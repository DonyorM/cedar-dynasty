(ns cedar-dynasty.game-test
  (:require [clojure.test :refer :all]
            [cedar-dynasty.game :as game]
            [cedar-dynasty.spec :as spec]
            [cedar-dynasty.test :refer [defspec-test]]))

(def test-uuid (parse-uuid "0f02b777-1269-4028-8025-d25b0166fad1"))

(deftest test-player-to-hand
  (testing "valid case"
    (is (= (game/player-to-hand {::spec/user-id test-uuid ::spec/name "Alice" ::spec/cards {:12 1, :11 1, :10 1, :4 1, :7 1, :1 1, :8 1, :9 1, :2 1, :5 1, :3 1, :6 1}})
           {::spec/user-id test-uuid ::spec/name "Alice" ::spec/count 12}))
    (is (= (game/player-to-hand {::spec/user-id test-uuid ::spec/name "Alice" ::spec/cards {}})
           {::spec/user-id test-uuid ::spec/name "Alice" ::spec/count 0})
        "Empty hand")))

(defspec-test spec-player-to-hand `game/player-to-hand)

