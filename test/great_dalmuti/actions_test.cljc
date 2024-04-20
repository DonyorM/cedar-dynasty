(ns great-dalmuti.actions-test
  (:require [clojure.test :refer [deftest is testing]]
            [great-dalmuti.actions :as sut]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.test :refer [defspec-test]]))

(defspec-test spec-play-valid `sut/play-valid)

(defspec-test spec-make-play `sut/make-play)

(defspec-test spec-play-valid-for-game `sut/play-valid-for-game)

(def existing-play
  {::spec/count 2 ::spec/card :4 ::spec/user-id #uuid"bdf49cf5-1b94-44aa-8b29-36103f5909ee"})

(deftest test-play-valid
  (testing "valid case"
    (is (sut/play-valid existing-play
                        {::spec/count 2 ::spec/card :3 ::spec/user-id #uuid"c2e1019d-6a7d-4171-8af6-0d71818e6fa9"}))
    (is (sut/play-valid {:great-dalmuti.main/user-id #uuid "b2f19947-76cf-45da-821c-26f15e2a1dc1", :great-dalmuti.spec/count 2, :great-dalmuti.spec/card :3}
                        {:great-dalmuti.spec/user-id #uuid "b2f19947-76cf-45da-821c-26f15e2a1dc2", :great-dalmuti.spec/count 2, :great-dalmuti.spec/card :3}))
    (is (sut/play-valid nil {::spec/count 2 ::spec/card :3 ::spec/user-id #uuid"c2e1019d-6a7d-4171-8af6-0d71818e6fa9"})
        "Nil play can always be overwriten"))
  (testing "invalid case"
    (is (not (sut/play-valid existing-play
                             {::spec/count 3 ::spec/card :3 ::spec/user-id #uuid"c2e1019d-6a7d-4171-8af6-0d71818e6fa9"}))
        "Wrong number of cards")
    (is (not (sut/play-valid existing-play
                             {:spec/count 2 ::spec/card :5 ::spec/user-id #uuid"c2e1019d-6a7d-4171-8af6-0d71818e6fa9"}))
        "Card not strong enough")))

(def game #:great-dalmuti.spec{:players        [#:great-dalmuti.spec{:user-id #uuid"d30255ec-3683-409b-99cb-9fa19e86458c",
                                                                     :name    "P7WSkvcIQ42yfOvQpFD126Rs1Ud",
                                                                     :cards   {:1 1, :2 2, :4 4, :12 3}}
                                                #:great-dalmuti.spec{:user-id #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407",
                                                                     :name    "xXN13rR2D6zz8wX6i2O3PWPJQ1",
                                                                     :cards   {:11 3,
                                                                               :4  1,
                                                                               :7  2,
                                                                               :1  3,
                                                                               :9  5,
                                                                               :2  1,
                                                                               :5  2,
                                                                               :3  1,
                                                                               :6  0}}
                                                #:great-dalmuti.spec{:user-id #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede",
                                                                     :name    "bHLrYPMs4k",
                                                                     :cards   {:6  3,
                                                                               :2  1,
                                                                               :7  2,
                                                                               :11 4,
                                                                               :1  1,
                                                                               :5  2,
                                                                               :8  6,
                                                                               :12 3}}],
                               :play           #:great-dalmuti.spec{:card    :8,
                                                                    :count   2,
                                                                    :user-id #uuid"d30255ec-3683-409b-99cb-9fa19e86458c"},
                               :current-player #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407"})

(deftest test-play-valid-for-game
  (testing "Valid play"
    (is (sut/play-valid-for-game game {::spec/user-id #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407" ::spec/count 2 ::spec/card :7})))

  (testing "invalid"
    (is (not (sut/play-valid-for-game game {::spec/user-id #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407" ::spec/count 2 ::spec/card :2}))
        "Not enough cards")
    (is (not (sut/play-valid-for-game game {::spec/user-id #uuid"d30255ec-3683-409b-99cb-9fa19e86458c" ::spec/count 2 ::spec/card :7}))
        "Wrong players turn")))

(deftest test-make-play
  (testing "Invalid play"
    (is (= game (sut/make-play game {::spec/user-id #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407" ::spec/count 2 ::spec/card :2}))))

  (testing "Valid play"
    (is (= #:great-dalmuti.spec{:players        [#:great-dalmuti.spec{:user-id #uuid"d30255ec-3683-409b-99cb-9fa19e86458c",
                                                                      :name    "P7WSkvcIQ42yfOvQpFD126Rs1Ud",
                                                                      :cards   {:1 1, :2 2, :4 4, :12 3}}
                                                 #:great-dalmuti.spec{:user-id #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407",
                                                                      :name    "xXN13rR2D6zz8wX6i2O3PWPJQ1",
                                                                      :cards   {:11 3,
                                                                                :4  1,
                                                                                :7  0,
                                                                                :1  3,
                                                                                :9  5,
                                                                                :2  1,
                                                                                :5  2,
                                                                                :3  1,
                                                                                :6  0}}
                                                 #:great-dalmuti.spec{:user-id #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede",
                                                                      :name    "bHLrYPMs4k",
                                                                      :cards   {:6  3,
                                                                                :2  1,
                                                                                :7  2,
                                                                                :11 4,
                                                                                :1  1,
                                                                                :5  2,
                                                                                :8  6,
                                                                                :12 3}}],
                                :play           {::spec/user-id #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407" ::spec/count 2 ::spec/card :7},
                                :current-player #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede"}
           (sut/make-play game {::spec/user-id #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407" ::spec/count 2 ::spec/card :7})))

    (is (= (-> game
               (assoc ::spec/current-player #uuid"d30255ec-3683-409b-99cb-9fa19e86458c")
               (assoc-in [::spec/players 2 ::spec/cards :5] 0)
               (assoc ::spec/play {::spec/user-id #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede" ::spec/count 2 ::spec/card :5}))
           (sut/make-play (assoc game ::spec/current-player #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede")
                          {::spec/user-id #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede" ::spec/count 2 ::spec/card :5}))
        "Current player wraps around to beginning")))
