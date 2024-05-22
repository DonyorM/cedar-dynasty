(ns great-dalmuti.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(def example-uuid #{#uuid"adb4085d-0c10-49df-8799-a1cd2b293dc6"
                    #uuid"83d6b098-8320-4456-81d4-efbd1647358f"
                    #uuid"2eb67d49-b56c-4786-a7e1-8c81163f26d5"
                    #uuid"85480a4d-8f9b-4628-9e38-39fa6e4393b1"
                    #uuid"3cf10c77-cb8b-40ad-ae81-0e9c5cf5831a"
                    #uuid"8be45883-12e5-4dde-b287-8251b0364b5c"
                    #uuid"6c24a532-c02e-49aa-8954-29e913ed8c4f"
                    #uuid"7f2ac39e-895e-46ce-bac6-282f344a9f32"
                    #uuid"a7ec0359-5383-473e-9462-d5296d131683"
                    #uuid"80055d89-aa29-47a3-92ad-5fcdad513eb3"
                    #uuid"0b6d47ae-2da8-43ce-a988-234e13b162ff"})

(defn player-in-game-gen
  []
  (gen/fmap
    (fn [[game play index-offset]]
      (let [players (::players game)
            index (mod index-offset (count players))]
        (list game (assoc play ::yser-id (-> players (nth index) ::user-id)))))
    (gen/tuple
      (s/gen ::game)
      (s/gen ::play)
      (gen/int))))

(def card-order [:1 :2 :3 :4 :5 :6 :7 :8 :9 :10 :11 :12])

(defn is-distinct [coll]
  (if (empty? coll)
    true
    (apply distinct? coll)))

(defn contains-player? [user-id players]
  (some #{user-id} (map ::user-id players)))

(s/def ::name string?)
(s/def ::user-id (s/with-gen uuid?
                             #(s/gen example-uuid)))
(s/def ::count nat-int?)
(s/def ::card (set card-order))
(s/def ::hand (s/keys :req [::user-id ::name ::count]))
(s/def ::cards (s/map-of ::card (s/and ::count
                                       ;; Since we will sum up all of the card counts to get the hand count
                                       ;; we don't want the total number of cards to overflow
                                       #(< % (/
                                               #?(:clj Long/MAX_VALUE)
                                               #?(:cljs (. js/Number -MAX_SAFE_INTEGER))
                                               (count card-order))))))
(s/def ::player (s/keys :req [::user-id ::name ::cards]))
(s/def ::play (s/nilable (s/and (s/keys :req [::card
                                              ::count
                                              ::user-id])
                                #(> (::count %) 0))))

(defn ^:private players-gen
  ([]
   (players-gen 0 (count example-uuid)))
  ([& opts]
   (gen/fmap
     (fn [[uuids players]]
       (mapv #(assoc %1 ::user-id %2) players uuids))
     (gen/tuple (gen/shuffle example-uuid)
                (apply gen/vector (s/gen ::player) opts)))))
(s/def ::players (s/with-gen (s/and (s/coll-of ::player :kind vector?)
                                    #(is-distinct (map ::user-id %)))
                             players-gen))
(s/def ::start-player ::user-id)
(s/def ::current-player ::user-id)
(s/def ::win-order (s/nilable (s/with-gen (s/and (s/coll-of ::user-id :kind vector?)
                                                 #(is-distinct (map ::user-id %)))
                                          #(gen/fmap
                                             (fn [[order size]]
                                               (take size order))
                                             (gen/tuple
                                               (gen/shuffle example-uuid)
                                               (gen/choose 0 (count example-uuid)))))))

(s/def ::from ::user-id)
(s/def ::to ::user-id)
(s/def ::card-debt (s/keys :req [::to ::count]))
(s/def ::card-debts (s/map-of ::user-id ::card-debt))
(s/def ::game (s/with-gen (s/and (s/keys :req [::players ::play ::current-player ::win-order ::card-debts])
                                 #(or (nil? (::play %))
                                      (contains-player? (-> % ::play ::user-id) (::players %)))
                                 #(contains-player? (::current-player %) (::players %)))
                          #(gen/fmap
                             (fn [[players play index]]
                               (let [current-player-index (if (= (count players) (inc index))
                                                            0
                                                            (inc index))]
                                 {::players        players
                                  ::play           (assoc play ::user-id (-> players
                                                                             (nth index)
                                                                             ::user-id))
                                  ::current-player (-> players
                                                       (nth current-player-index)
                                                       ::user-id)
                                  ::win-order      (->> players
                                                   (filter (fn [player] (= 0 (apply + (vals (::cards player))))))
                                                   ::user-id
                                                   reverse
                                                   vec)
                                  ::card-debts     {}}))
                             (gen/bind (gen/choose 2 (count example-uuid))
                                       (fn [size]
                                         (gen/tuple
                                           (players-gen size)
                                           (s/gen ::play)
                                           (gen/choose 0 (dec size))))))))