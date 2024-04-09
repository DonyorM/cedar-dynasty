(ns great-dalmuti.main
  (:require [great-dalmuti.actions :as a]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.game :refer [Game]]))

;; Saving this file will automatically recompile and update in your browser

(def bob-id (random-uuid))
(def !game
  (atom {::spec/players [{::spec/user-id bob-id
                          ::spec/name    "Bob Jones"
                          ::spec/cards   {:1 1, :2 3, :3 2, :4 1}}
                         {::spec/user-id (random-uuid)
                          ::spec/name    "Jane Eyre"
                          ::spec/cards   {:1 1, :2 1, :3 2, :4 2, :5 2}}
                         {::spec/user-id (random-uuid)
                          ::spec/name    "Jerry Foster"
                          ::spec/cards   {:1 1, :2 1, :3 2, :4 1, :6 3, :12 4}}]
         ::spec/play    {::user-id bob-id ::spec/count 2 ::spec/card :3}})
  )


(e/defn Main [ring-request]
  (e/client
    (binding [dom/node js/document.body]
      #_(Outer.)
      (dom/div (dom/props {:class "min-h-screen bg-sky-800 text-white h-screen"})
               (e/server
                 (Game. !game bob-id))))))
