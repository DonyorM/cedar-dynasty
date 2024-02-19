(ns great-dalmuti.components
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]))

(e/defn Card [num]
  (e/client
    (dom/div :w-32 (dom/props {:style {:border "1px solid black"}})
      (dom/span (dom/text num)))))