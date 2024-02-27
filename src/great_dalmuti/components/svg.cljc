(ns great-dalmuti.components.svg
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-svg :as svg]))

(e/defn HandIcon [{class-names :class}]
  (e/client
    (svg/svg (dom/props {:class class-names :width "71" :height "70" :viewBox "0 0 71 70" :fill "none"})
             (svg/rect (dom/props {:x "0.675707" :y "0.224052" :width "33.6954" :height "55.2488" :rx "4.5" :transform "matrix(0.896741 -0.442555 0.454672 0.890659 -0.0320974 15.6781)" :fill "#EA8949" :stroke "black"}))
             (svg/rect (dom/props {:x "0.592672" :y "0.38998" :width "33.7884" :height "55.0945" :rx "4.5" :transform "matrix(0.980052 -0.19874 0.205292 0.978701 14.2466 8.41613)" :fill "#EA8949" :stroke "black"}))
             (svg/rect (dom/props {:x "0.381352" :y "0.592511" :width "33.786" :height "55.0984" :rx "4.5" :transform "matrix(0.978027 0.20848 -0.215323 0.976543 37.1143 7.89973)" :fill "#EA8949" :stroke "black"})))))