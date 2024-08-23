(ns cedar-dynasty.components.button
  (:require [cedar-dynasty.spec :as spec]
            [hyperfiddle.electric-svg :as svg]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-ui4 :as ui]
            [hyperfiddle.electric-dom2 :as dom]))

(e/defn Button
  [{:keys [on-click text disabled]
    class :class
    :or {class ""}}]
  (e/client
    (ui/button
      on-click
      (dom/props {:class (str "rounded-lg bg-cyan-600 hover:bg-cyan-400 py-2 px-4 font-bold disabled:bg-gray-400 disabled:text-gray-800" class)
                  :disabled disabled})
      (dom/text text))))