(ns cedar-dynasty.backend.auth
  (:require [com.github.sikt-no.clj-jwt :as clj-jwt]
            [cedar-dynasty.backend.config :as config]))

(defn decode-jwt
  ([key-url message]
   (clj-jwt/unsign key-url message))
  ([message]
   (decode-jwt config/COGNITO_KEYS_URL message)))

(defn get-user-id
  [id-token]
  (-> id-token
      decode-jwt
      :sub
      parse-uuid))