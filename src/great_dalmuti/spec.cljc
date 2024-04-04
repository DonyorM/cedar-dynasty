(ns great-dalmuti.spec
  (:require [clojure.spec.alpha :as s]))

(def card-order [:1 :2 :3 :4 :5 :6 :7 :8 :9 :10 :11 :12])

(s/def ::name string?)
(s/def ::user-id uuid?)
(s/def ::count nat-int?)
(s/def ::card (set card-order))
(s/def ::hand (s/keys :req [::user-id ::name ::count]))
(s/def ::cards (s/map-of ::card (s/and ::count
                                       ;; Since we will sum up all of the card counts to get the hand count
                                       ;; we don't want the total number of cards to overflow
                                       #(< % (/ Long/MAX_VALUE (count card-order))))))
(s/def ::player (s/keys :req [::user-id ::name ::cards]))
(s/def ::play (s/keys :req [::card ::count ::user-id]))
(s/def ::players (s/coll-of ::player))
(s/def ::game (s/keys :req [::players ::play]))