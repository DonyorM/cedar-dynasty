(ns great-dalmuti.main
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [great-dalmuti.components.hand :refer [Hand]]
            [great-dalmuti.components :refer [Card]]))

;; Saving this file will automatically recompile and update in your browser

(e/defn Main [ring-request]
  (e/client
    (binding [dom/node js/document.body]
      (dom/div (dom/props {:class "min-h-screen bg-sky-800 text-white"})
               (Hand. {:user-id "123" :name "Alice" :count 13})
               (dom/h1 (dom/text "Hello from Electric Clojure"))
               (dom/p (dom/text "Source code for this page is in ")
                      (dom/code (dom/text "src/electric_start_app/main.cljc")))
               (dom/p (dom/text "Make sure you check the ")
                      (Card. 1)
                      (dom/a (dom/props {:href "https://electric.hyperfiddle.net/" :target "_blank"})
                             (dom/text "Electric Tutorial")))))))
