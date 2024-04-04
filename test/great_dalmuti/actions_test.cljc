(ns great-dalmuti.actions-test
  (:require [clojure.test :refer [deftest is testing]]
            [great-dalmuti.actions :as sut]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.test :refer [defspec-test]]))

(defspec-test spec-play-valid `sut/play-valid)

(defspec-test spec-make-play `sut/make-play)

(def existing-play
  {::spec/count 2 ::spec/card :4 ::spec/user-id #uuid"bdf49cf5-1b94-44aa-8b29-36103f5909ee"})
(deftest test-play-valid
  (testing "valid case"
    (is (sut/play-valid existing-play
                      {::spec/count 2 ::spec/card :3 ::spec/user-id #uuid"c2e1019d-6a7d-4171-8af6-0d71818e6fa9"})))
  (testing "invalid case"
    (is (not (sut/play-valid existing-play
                           {::spec/count 3 ::spec/card :3 ::spec/user-id #uuid"c2e1019d-6a7d-4171-8af6-0d71818e6fa9"}))
        "Wrong number of cards")
    (is (not (sut/play-valid existing-play
                           {:spec/count 2 ::spec/card :5 ::spec/user-id #uuid"c2e1019d-6a7d-4171-8af6-0d71818e6fa9"}))
        "Card not strong enough")))
