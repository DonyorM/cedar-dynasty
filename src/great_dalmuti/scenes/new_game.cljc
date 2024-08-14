(ns great-dalmuti.scenes.new-game
  (:require [great-dalmuti.utils.routes :as routes]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-ui4 :as ui4]
            [great-dalmuti.components.button :refer [Button]]
            [contrib.str :refer [empty->nil]]
            [hyperfiddle.electric-dom2 :as dom]
            [great-dalmuti.utils.routes :as routes]
            [contrib.electric-goog-history :as history]))

(e/defn NewGame []
  (e/client
    (let [!game-code (atom "")]
      (dom/div (dom/props {:class "h-full flex flex-col justify-center items-center gap-4"})
               (dom/label (dom/props {:class "flex gap-2"})
                 (dom/text "Enter Game Code:")
                          (dom/input
                            (dom/props {:placeholder "" :maxlength 100 :class "text-black"})
                            (dom/on "keydown" (e/fn [e]
                                                (when (= "Enter" (.-key e))
                                                  (when-some [v (empty->nil (.substr (.. e -target -value) 0 100))]
                                                    (routes/Navigate. :game {:path-params {:code v}})))
                                                (reset! !game-code (.-value dom/node))))))
               (Button. {:text "New Game"
                 :on-click (e/fn [e] (routes/Navigate. :game {:path-params {:code @!game-code}}))})))))