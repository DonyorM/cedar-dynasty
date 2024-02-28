(ns great-dalmuti.spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::name string?)
(s/def ::user-id uuid?)
(s/def ::count int?)

(s/def ::card #{:1 :2 :3 :4 :5 :6 :7 :8 :9 :10 :11 :12})
(s/def ::hand (s/keys :req [::user-id ::name ::count]))
(s/def ::cards (s/coll-of ::card))
(s/def ::player (s/keys :req [::user-id ::name ::cards]))
(s/def ::players (s/coll-of ::player))
(s/def ::game (s/keys :req [::players]))