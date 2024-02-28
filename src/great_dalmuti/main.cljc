(ns great-dalmuti.main
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [great-dalmuti.spec :as spec]
            [great-dalmuti.game :refer [Game]]))

;; Saving this file will automatically recompile and update in your browser

(def game
  {::spec/players [{::spec/uuid (random-uuid)
                    ::spec/name "Bob Jones"
                    ::spec/cards [:1 :2 :3 :3 :4]}
                   {::spec/uuid (random-uuid)
                    ::spec/name "Jane Eyre"
                    ::spec/cards [:1 :2 :3 :3 :4 :4 :5 :5]}
                   {::spec/uuid (random-uuid)
                    ::spec/name "Jerry Foster"
                    ::spec/cards [:1 :2 :3 :3 :4 :6 :6 :6 :12 :12 :12 :!2]}]})

(e/defn Main [ring-request]
  (e/client
    (binding [dom/node js/document.body]
      (dom/div (dom/props {:class "min-h-screen bg-sky-800 text-white"})
               (Game. game)))))
