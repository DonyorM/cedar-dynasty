(ns great-dalmuti.actions-test
  (:require [clojure.test :refer [deftest is testing]]
            [great-dalmuti.actions :as sut]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.test :refer [defspec-test]]
            [great-dalmuti.utils :as u]))

(defspec-test spec-play-valid `sut/play-valid)

(defspec-test spec-make-play `sut/make-play)

(defspec-test spec-play-valid-for-game `sut/play-valid-for-game)

(defspec-test spec-add-new-player `sut/upsert-player)

(defspec-test spec-deal-cards `sut/deal-cards)

(defspec-test spec-move-cards `sut/move-cards)

(defspec-test spec-tax-player `sut/tax-player)

(defspec-test spec-skip `sut/skip)

(defspec-test spec-start-new-round `sut/start-new-round)

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
                               :current-player #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407"
                               :card-debts     {}
                               :win-order      []})

(deftest test-play-valid-for-game
  (testing "Valid play"
    (is (sut/play-valid-for-game game {::spec/user-id #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407" ::spec/count 2 ::spec/card :7})))

  (testing "invalid"
    (is (not (sut/play-valid-for-game game {::spec/user-id #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407" ::spec/count 2 ::spec/card :2}))
        "Not enough cards")
    (is (not (sut/play-valid-for-game game {::spec/user-id #uuid"d30255ec-3683-409b-99cb-9fa19e86458c" ::spec/count 2 ::spec/card :7}))
        "Wrong players turn")))

(def game-final-player )

(def after-play-expectation)

(deftest test-make-play
  (testing "Invalid play"
    (is (= game (sut/make-play game {::spec/user-id #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407" ::spec/count 2 ::spec/card :2}))))

  (testing "Valid play"
    (is (= (merge game #:great-dalmuti.spec{:players        [#:great-dalmuti.spec{:user-id #uuid"d30255ec-3683-409b-99cb-9fa19e86458c",
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
                                            :current-player #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede"})
           (sut/make-play game {::spec/user-id #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407" ::spec/count 2 ::spec/card :7})))

    (is (= (-> game
               (assoc ::spec/current-player #uuid"d30255ec-3683-409b-99cb-9fa19e86458c")
               (assoc-in [::spec/players 2 ::spec/cards :5] 0)
               (assoc ::spec/play {::spec/user-id #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede" ::spec/count 2 ::spec/card :5}))
           (sut/make-play (assoc game ::spec/current-player #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede")
                          {::spec/user-id #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede" ::spec/count 2 ::spec/card :5}))
        "Current player wraps around to beginning")

    (let [given-game #:great-dalmuti.spec{:players
                                          [#:great-dalmuti.spec{:user-id
                                                                #uuid "73d37285-326e-4d95-bab7-caa930c81e27",
                                                                :name "donyor",
                                                                :cards
                                                                {:12 0,
                                                                 :10 0,
                                                                 :8 0,
                                                                 :7 0,
                                                                 :11 0,
                                                                 :6 0,
                                                                 :5 0}}
                                           #:great-dalmuti.spec{:user-id
                                                                #uuid "417acb5d-3d48-4104-ad6b-24df9092d681",
                                                                :name "dsa",
                                                                :cards
                                                                {:12 5,
                                                                 :11 2,
                                                                 :10 0,
                                                                 :4 0,
                                                                 :7 0,
                                                                 :8 0,
                                                                 :9 0,
                                                                 :2 0,
                                                                 :5 2,
                                                                 :3 0,
                                                                 :6 0}}
                                           #:great-dalmuti.spec{:user-id
                                                                #uuid "8e082568-abc5-41e5-a55f-92b4062e1a7f",
                                                                :name "bob",
                                                                :cards
                                                                {:12 0,
                                                                 :11 0,
                                                                 :10 0,
                                                                 :4 0,
                                                                 :7 0,
                                                                 :1 0,
                                                                 :9 0,
                                                                 :2 0,
                                                                 :5 1,
                                                                 :3 0,
                                                                 :6 0}}],
                                          :play
                                          #:great-dalmuti.spec{:user-id
                                                               #uuid "417acb5d-3d48-4104-ad6b-24df9092d681",
                                                               :card :6,
                                                               :count 1},
                                          :current-player
                                          #uuid "8e082568-abc5-41e5-a55f-92b4062e1a7f",
                                          :win-order
                                          [#uuid "73d37285-326e-4d95-bab7-caa930c81e27"]}
          expected-game #:great-dalmuti.spec{:players
                                             [#:great-dalmuti.spec{:user-id
                                                                   #uuid "73d37285-326e-4d95-bab7-caa930c81e27",
                                                                   :name "donyor",
                                                                   :cards
                                                                   {:12 0,
                                                                    :10 0,
                                                                    :8  0,
                                                                    :7  0,
                                                                    :11 0,
                                                                    :6  0,
                                                                    :5  0}}
                                              #:great-dalmuti.spec{:user-id
                                                                   #uuid "417acb5d-3d48-4104-ad6b-24df9092d681",
                                                                   :name "dsa",
                                                                   :cards
                                                                   {:12 5,
                                                                    :11 2,
                                                                    :10 0,
                                                                    :4  0,
                                                                    :7  0,
                                                                    :8  0,
                                                                    :9  0,
                                                                    :2  0,
                                                                    :5  2,
                                                                    :3  0,
                                                                    :6  0}}
                                              #:great-dalmuti.spec{:user-id
                                                                   #uuid "8e082568-abc5-41e5-a55f-92b4062e1a7f",
                                                                   :name "bob",
                                                                   :cards
                                                                   {:12 0,
                                                                    :11 0,
                                                                    :10 0,
                                                                    :4  0,
                                                                    :7  0,
                                                                    :1  0,
                                                                    :9  0,
                                                                    :2  0,
                                                                    :5  0,
                                                                    :3  0,
                                                                    :6  0}}],
                                             :play
                                             #:great-dalmuti.spec{:user-id
                                                                  #uuid "8e082568-abc5-41e5-a55f-92b4062e1a7f",
                                                                  :card  :5,
                                                                  :count 1},
                                             :current-player
                                             #uuid "417acb5d-3d48-4104-ad6b-24df9092d681",
                                             :win-order
                                             [#uuid "73d37285-326e-4d95-bab7-caa930c81e27"
                                              #uuid "8e082568-abc5-41e5-a55f-92b4062e1a7f"
                                              #uuid "417acb5d-3d48-4104-ad6b-24df9092d681"]}
          given-play #:great-dalmuti.spec{:user-id
                                          #uuid "8e082568-abc5-41e5-a55f-92b4062e1a7f",
                                          :card  :5,
                                          :count 1}]
      (is (= expected-game
             (sut/make-play given-game given-play))
          "Handle situation where player loses"))

    (is (= #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407"
           (::spec/current-player (sut/make-play (-> game
                                                     (assoc-in [::spec/players 0 ::spec/cards] {})
                                                     (assoc ::spec/win-order [#uuid"d30255ec-3683-409b-99cb-9fa19e86458c"])
                                                     (assoc ::spec/current-player #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede"))
                                                 {::spec/user-id #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede" ::spec/count 2 ::spec/card :5})))
        "Skips player with no cards")

    (is (= [#uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede"]
           (::spec/win-order (sut/make-play (-> game
                                                (assoc-in [::spec/players 2 ::spec/cards] {:5 2})
                                                (assoc ::spec/current-player #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede"))
                                            {::spec/user-id #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede" ::spec/count 2 ::spec/card :5})))
        "Adds user with 0 cards to win list")

    (let [starting-situation (-> game
                                 (assoc-in [::spec/players 1 ::spec/cards] {:1 1})
                                 (assoc-in [::spec/players 2 ::spec/cards] {})
                                 (assoc ::spec/win-order []))])))

(deftest test-upsert-player
  (testing "upserting a player"
    (is (= (::spec/players (sut/upsert-player
                             (assoc game ::spec/players [#:great-dalmuti.spec{:user-id #uuid"d30255ec-3683-409b-99cb-9fa19e86458c",
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
                                                                                        :6  0}}])
                             #uuid"f489f09f-c1fd-4d9a-9ef4-b9164f3fa898"
                             "Johnny Applessed"))
           [#:great-dalmuti.spec{:user-id #uuid"d30255ec-3683-409b-99cb-9fa19e86458c",
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
            #:great-dalmuti.spec{:user-id #uuid"f489f09f-c1fd-4d9a-9ef4-b9164f3fa898",
                                 :name    "Johnny Applessed",
                                 :cards   {}}])
        "adding a new player")
    (is (= (::spec/players (sut/upsert-player
                             (assoc game ::spec/players [#:great-dalmuti.spec{:user-id #uuid"d30255ec-3683-409b-99cb-9fa19e86458c",
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
                                                                                        :6  0}}])
                             #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407"
                             "Johnny Applessed"))
           [#:great-dalmuti.spec{:user-id #uuid"d30255ec-3683-409b-99cb-9fa19e86458c",
                                 :name    "P7WSkvcIQ42yfOvQpFD126Rs1Ud",
                                 :cards   {:1 1, :2 2, :4 4, :12 3}}
            #:great-dalmuti.spec{:user-id #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407",
                                 :name    "Johnny Applessed",
                                 :cards   {:11 3,
                                           :4  1,
                                           :7  2,
                                           :1  3,
                                           :9  5,
                                           :2  1,
                                           :5  2,
                                           :3  1,
                                           :6  0}}])
        "updating players name")))

(deftest deal-cards
  (testing "Deals cards consistently"
    (is (= (sut/deal-cards #:great-dalmuti.spec{:players [#:great-dalmuti.spec{:user-id #uuid "adb4085d-0c10-49df-8799-a1cd2b293dc6", :name "", :cards {}} #:great-dalmuti.spec{:user-id #uuid "7f2ac39e-895e-46ce-bac6-282f344a9f32", :name "", :cards {}}],
                                                :play    #:great-dalmuti.spec{:card :12, :count 1, :user-id #uuid "adb4085d-0c10-49df-8799-a1cd2b293dc6"}, :current-player #uuid "7f2ac39e-895e-46ce-bac6-282f344a9f32"},
                           [:12 :12 :12 :12])
           #:great-dalmuti.spec{:players [#:great-dalmuti.spec{:user-id #uuid "adb4085d-0c10-49df-8799-a1cd2b293dc6", :name "", :cards {:12 2}} #:great-dalmuti.spec{:user-id #uuid "7f2ac39e-895e-46ce-bac6-282f344a9f32", :name "", :cards {:12 2}}],
                                :play    nil, :current-player #uuid "7f2ac39e-895e-46ce-bac6-282f344a9f32"}))))

(deftest move-cards
  (let [from-player #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede"
        to-player #uuid"d30255ec-3683-409b-99cb-9fa19e86458c"]
    (testing "Successful payments"

      (is (= (-> game
                 (assoc-in [::spec/players 0 ::spec/cards :6] 1)
                 (assoc-in [::spec/players 2 ::spec/cards :6] 2)
                 (assoc ::spec/card-debts {from-player {::spec/to to-player ::spec/count 1}}))
             (sut/move-cards
               (assoc game ::spec/card-debts {from-player {::spec/to to-player ::spec/count 2}})
               from-player
               {:6 1}))
          "Move single card")
      (is (= (-> game
                 (assoc-in [::spec/players 0 ::spec/cards :6] 1)
                 (assoc-in [::spec/players 0 ::spec/cards :12] 4)
                 (assoc-in [::spec/players 2 ::spec/cards :6] 2)
                 (assoc-in [::spec/players 2 ::spec/cards :12] 2)
                 (assoc ::spec/card-debts {}))
             (sut/move-cards
               (assoc game ::spec/card-debts {from-player {::spec/to to-player ::spec/count 2}})
               from-player
               {:6 1 :12 1}))
          "Move both cards")
      (testing "Error messages"
        (is (= "That's too many cards. You only owe 2"
               (u/get-errors
                 (sut/move-cards (assoc game ::spec/card-debts {from-player {::spec/to to-player ::spec/count 2}})
                                 from-player
                                 {:12 3}))))
        (is (= "You don't have enough cards to give that"
               (u/get-errors
                 (sut/move-cards (assoc game ::spec/card-debts {from-player {::spec/to to-player ::spec/count 2}})
                                 from-player
                                 {:2 2}))))
        (is (= "You don't owe any cards"
               (u/get-errors
                 (sut/move-cards (assoc game ::spec/card-debts {to-player {::spec/to from-player ::spec/count 2}})
                                 from-player
                                 {:12 2}))))))))

(deftest skip
  (is (= (assoc game ::spec/current-player #uuid "12c21aec-6771-4dd4-be0c-43e3ebb09ede")
         (sut/skip game))
      "Skips an ordinary player")
  (is (= (assoc game ::spec/current-player #uuid"d30255ec-3683-409b-99cb-9fa19e86458c"
                     ::spec/play nil)
         (sut/skip (assoc game ::spec/current-player #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede")))
      "Resets play if reaching original player")
  (let [start-game (assoc-in game [::spec/players 2 ::spec/cards] {})]
    (is (= (assoc start-game ::spec/current-player #uuid"d30255ec-3683-409b-99cb-9fa19e86458c"
                             ::spec/play nil)
           (sut/skip start-game))
        "Jumps over players with no cards"))
  (let [given-game (-> game
                       (assoc-in [::spec/players 0 ::spec/cards] {})
                       (update ::spec/win-order conj (get-in game [::spec/players 0 ::spec/user-id]))
                       (assoc ::spec/current-player #uuid"12c21aec-6771-4dd4-be0c-43e3ebb09ede"))
        expected-game (-> given-game
                          (assoc ::spec/current-player #uuid"198e2856-95e6-42c0-93fb-7cd57ca50407")
                          (assoc ::spec/play nil))]
    (is (= expected-game
           (sut/skip given-game))
        "Moves to next player if original player has no cards")))