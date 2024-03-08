(ns great-dalmuti.components.button
  (:require [great-dalmuti.spec :as spec]
            [hyperfiddle.electric-svg :as svg]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-ui4 :as ui]
            [hyperfiddle.electric-dom2 :as dom]))

(e/defn Button
  [{:keys [on-click text]
    class :class
    :or {class ""}}]
  (e/client
    (ui/button
      on-click
      (dom/props {:class (str "rounded-lg bg-cyan-600 hover:bg-cyan-400 py-2 px-4 font-bold" class)})
      (dom/text text))))