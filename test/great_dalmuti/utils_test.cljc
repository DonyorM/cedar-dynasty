(ns great-dalmuti.utils-test
  (:require [great-dalmuti.test :refer [defspec-test]]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.utils :as u]
            [clojure.test :refer :all]))

(defspec-test spec-user-for-id `u/user-for-id)

(def game {:great-dalmuti.spec/players
           [{:great-dalmuti.spec/user-id #uuid "572c8f38-166d-4e5f-a5ec-b6f125388b5e", :great-dalmuti.spec/name "Bob Jones", :great-dalmuti.spec/cards {:1 1, :2 1, :3 2, :4 1}}
            {:great-dalmuti.spec/user-id #uuid "0be0d1a6-bdf3-49ae-b926-a71f43ba116a", :great-dalmuti.spec/name "Jane Eyre", :great-dalmuti.spec/cards {:1 1, :2 1, :3 2, :4 2, :5 2}}
            {:great-dalmuti.spec/user-id #uuid "ee1b938c-1a83-4e15-9111-c6ce9ee3c318", :great-dalmuti.spec/name "Jerry Foster", :great-dalmuti.spec/cards {:1 1, :2 1, :3 2, :4 1, :6 3, :12 4}}],
           :great-dalmuti.spec/play {:great-dalmuti.main/user-id #uuid "572c8f38-166d-4e5f-a5ec-b6f125388b5e", :great-dalmuti.spec/count 2, :great-dalmuti.spec/card :3}})

(def target-player {:great-dalmuti.spec/user-id #uuid "572c8f38-166d-4e5f-a5ec-b6f125388b5e", :great-dalmuti.spec/name "Bob Jones", :great-dalmuti.spec/cards {:1 1, :2 1, :3 2, :4 1}})

(deftest user-for-id
  (testing "Returns a valid user"
    (is (= target-player (u/user-for-id game #uuid "572c8f38-166d-4e5f-a5ec-b6f125388b5e")))))