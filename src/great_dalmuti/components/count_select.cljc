(ns great-dalmuti.components.count-select
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-ui4 :as ui]))

(def selected-class "border-blue-300 border-2")

(e/defn CountSelect
  [selected-count on-count-select max-value]
  (e/client
    (dom/div (dom/props {:class "overflow-x-scroll mx-4 flex gap-2"})
             (e/for [num (range 1 (inc max-value))]
               (ui/button (e/fn [] (on-count-select num))
                          (dom/props {:class "w-8 h-8 bg-slate-700 rounded flex align-center justify-center flex-row"})
                          (when (= num selected-count)
                            (dom/props {:class selected-class}))
                          (dom/span (dom/props {:class "text-lg"})
                          (dom/text num)))))))