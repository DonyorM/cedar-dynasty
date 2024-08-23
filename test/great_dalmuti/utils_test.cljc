(ns cedar-dynasty.utils-test
  (:require [cedar-dynasty.test :refer [defspec-test]]
            [cedar-dynasty.spec :as spec]
            [cedar-dynasty.utils :as u]
            [clojure.test :refer :all]))

(defspec-test spec-user-for-id `u/user-for-id)

(defspec-test spec-card-count `u/card-count)

(def game {:cedar-dynasty.spec/players
           [{:cedar-dynasty.spec/user-id #uuid "572c8f38-166d-4e5f-a5ec-b6f125388b5e", :cedar-dynasty.spec/name "Bob Jones", :cedar-dynasty.spec/cards {:1 1, :2 1, :3 2, :4 1}}
            {:cedar-dynasty.spec/user-id #uuid "0be0d1a6-bdf3-49ae-b926-a71f43ba116a", :cedar-dynasty.spec/name "Jane Eyre", :cedar-dynasty.spec/cards {:1 1, :2 1, :3 2, :4 2, :5 2}}
            {:cedar-dynasty.spec/user-id #uuid "ee1b938c-1a83-4e15-9111-c6ce9ee3c318", :cedar-dynasty.spec/name "Jerry Foster", :cedar-dynasty.spec/cards {:1 1, :2 1, :3 2, :4 1, :6 3, :12 4}}],
           :cedar-dynasty.spec/play {:cedar-dynasty.main/user-id #uuid "572c8f38-166d-4e5f-a5ec-b6f125388b5e", :cedar-dynasty.spec/count 2, :cedar-dynasty.spec/card :3}})

(def target-player {:cedar-dynasty.spec/user-id #uuid "572c8f38-166d-4e5f-a5ec-b6f125388b5e", :cedar-dynasty.spec/name "Bob Jones", :cedar-dynasty.spec/cards {:1 1, :2 1, :3 2, :4 1}})

(deftest user-for-id
  (testing "Returns a valid user"
    (is (= target-player (u/user-for-id game #uuid "572c8f38-166d-4e5f-a5ec-b6f125388b5e")))))