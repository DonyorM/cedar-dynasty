(ns great-dalmuti.components.card
  (:require [great-dalmuti.spec :as spec]
            [hyperfiddle.electric-svg :as svg]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]))

(def card-class "top-2 absolute border border-solid border-black w-28 h-48 bg-orange-400 rounded-md flex justify-center items-center ")

(def selected-class "border-blue-300 border-4")

(e/defn Card
  [type count {:keys [selected on-click]}]
  (e/client
    (println "on-click for" type on-click)
    (dom/div
      (when on-click (dom/on! "click" #(on-click type)))
      (when on-click (dom/on! "tap" #(on-click type)))
      (dom/div (dom/props {:class "w-28 text-center"}) (dom/text (str "Count: " count)))
      (dom/div (dom/props {:class "relative w-40 h-52"})
               (dom/div (dom/props {:class card-class
                                    :style {:z-index count}})
                        (when selected
                          (dom/props {:class selected-class}))
                        (dom/div (dom/props {:class "text-8xl"}) (dom/text (subs (str type) 1))))
               (e/for [val (range 1 count)]
                 (let [rotation (* 6 val)]
                   (dom/div (dom/props {:class (str card-class "origin-bottom")
                                        :style {:transform (str "rotate(" rotation "deg)")
                                                :z-index   (- count val)}}))))))))
