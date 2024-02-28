(ns great-dalmuti.game-test
  (:require [clojure.spec.alpha :as s]
            [great-dalmuti.game :as game]
            [great-dalmuti.spec :as spec]
            [clojure.test :refer :all]
            [clojure.test.check.properties :as prop]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test.check.clojure-test :refer [defspec]]))

(def test-uuid (parse-uuid "0f02b777-1269-4028-8025-d25b0166fad1"))

(deftest test-player-to-hand
  (testing "valid case"
    (is (= (game/player-to-hand {::spec/user-id test-uuid ::spec/name "Alice" ::spec/cards #{:1 :2 :3 :4 :5 :6 :7 :8 :9 :10 :11 :12}})
           {::spec/user-id test-uuid ::spec/name "Alice" ::spec/count 12}))
    (is (= (game/player-to-hand {::spec/user-id test-uuid ::spec/name "Alice" ::spec/cards #{}})
           {::spec/user-id test-uuid ::spec/name "Alice" ::spec/count 0})
        "Empty hand")))

(defspec player-to-hand-generates-valid-hands 50
  (prop/for-all [player (s/gen ::spec/player)]
    (let [hand (game/player-to-hand player)]
      (and (s/valid? ::spec/hand hand)
           (= (::spec/count hand) (count (::spec/cards player)))))))

