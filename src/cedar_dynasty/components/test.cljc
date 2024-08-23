(ns cedar-dynasty.components.test
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]))

(e/defn Inner [v handler]
  (e/client
    (dom/div (when handler (dom/on! "click" handler))
             (dom/text v "Click me"))))

(e/defn Middle [pass-handler]
  (e/server
    (e/for-by identity [v (range 10)]
              (e/client
                (Inner. v #(when pass-handler (pass-handler)))))))

(e/defn Outer []
  (e/client (let [h #(println "Does this work?")]
              (Middle. h))))