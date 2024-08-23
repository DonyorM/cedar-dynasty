(ns cedar-dynasty.backend.db
  (:require [taoensso.faraday :as far]
            [cedar-dynasty.spec :as spec])
  (:import (com.amazonaws AmazonServiceException)))

(def client-opts
  {;;; For DynamoDB Local just use some random strings here, otherwise include your
   ;;; production IAM keys:
   :access-key (System/getenv "AWS_ACCESS_KEY")
   :secret-key (System/getenv "AWS_SECRET_KEY")

   :endpoint (System/getenv "DYNAMODB_ENDPOINT")
   ;;; You may optionally override the default endpoint if you'd like to use DynamoDB
   ;;; Local or a different AWS Region (Ref. http://goo.gl/YmV80o), etc.:
   ;; :endpoint "http://localhost:8000"                   ; For DynamoDB Local
   ;; :endpoint "http://dynamodb.eu-west-1.amazonaws.com" ; For EU West 1 AWS region

   ;;; You may optionally provide your own (pre-configured) instance of the Amazon
   ;;; DynamoDB client for Faraday functions to use.
   ;; :client (AmazonDynamoDBClientBuilder/defaultClient)
   })

(defonce !current-games (atom {}))

(def REQUIRED_TABLES [:games :users])

(defn setup-tables! []
  (far/create-table client-opts "games" [:id :s] {:block? true, :throughput {:read 1 :write 1}})
  (far/create-table client-opts "users" [:id :s] {:block? true, :throughput {:read 1 :write 1}}))

(defn delete-tables! []
  (far/delete-table client-opts "games")
  (far/delete-table client-opts "users"))

(defn- save-game!
  [game-code game]
  (far/put-item client-opts
                :games
                {:id   game-code
                 :data (far/freeze game)}))

(defn- get-game-from-db
  [game-code]
  (try
    (:data (far/get-item client-opts :games {:id game-code}))
    (catch AmazonServiceException e
      (println (.getMessage e))
      nil)))

(defn reset-game!
  [game-code game]
  (swap! !current-games assoc game-code game)
  (save-game! game-code game))

(defn get-game
  [game-code]
  (if-let [game (get @!current-games game-code)]
    game
    (let [db-game (get-game-from-db game-code)]
      (swap! !current-games assoc game-code db-game)
      db-game)))

(defn swap-game!
  [game-code f & args]
  (comment (db/swap-game! game-code update ::spec/players conj {::spec/user-id current-player-id
                                                                ::spec/name    player-name
                                                                ::spec/cards   {}}))
  (let [result (swap! !current-games (fn [current-games]
                                       (update current-games game-code #(apply f % args))))]
    (save-game! game-code (get result game-code))
    (get-game result)))

(let [tables (far/list-tables client-opts)]
  (when (not-every? #(some #{%} tables) REQUIRED_TABLES)
    (setup-tables!)))

(defn get-user [user-id]
  (try
    (far/get-item client-opts :users {:id (str user-id)})
    (catch AmazonServiceException e
      (println (.getMessage e))
      nil)))

(defn set-user-name [user-id name]
  (far/put-item client-opts :users {:id (str user-id) :name name}))