(ns cedar-dynasty.components.hand
  (:require [cedar-dynasty.components.svg :refer [HandIcon]]
            [cedar-dynasty.spec :as spec]
            [hyperfiddle.electric-svg :as svg]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]))

(e/defn Hand [hand is-current]
  (e/client
    (dom/div (dom/props {:class "flex flex-col items-center"})
      (svg/svg (dom/props {:fill "none" :viewBox "0 0 24 24" :stroke-width "1.5" :stroke "currentColor" :class (str (if is-current "text-white" "text-black") " w-20")})
               (svg/path (dom/props {:stroke-linecap "round" :stroke-linejoin "round"
                                     :d              "M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z"})))
      (dom/div (dom/text (::spec/name hand)))
      (dom/div (dom/props {:class "grid"})
               (dom/span (dom/props {:class "mt-2 text-white font-family-sans text-center font-bold text-5xl col-start-1 row-start-1 z-10 drop-shadow-[0_1.2px_1.2px_rgba(0,0,0,0.8)]"}) (dom/text (::spec/count hand)))
               (HandIcon. {:class "col-start-1 row-start-1"})))))
