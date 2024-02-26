(ns great-dalmuti.types)

(s/def ::name string?)
(s/def ::user-id uuid?)
(s/def ::hand (s/keys :req [::id ::name ::email]))