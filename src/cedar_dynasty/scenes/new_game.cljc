(ns cedar-dynasty.scenes.new-game
  (:require [cedar-dynasty.utils.routes :as routes]
            [hyperfiddle.electric :as e]
            [cedar-dynasty.components.button :refer [Button]]
            [contrib.str :refer [empty->nil]]
            [hyperfiddle.electric-dom2 :as dom]
            [cedar-dynasty.utils.routes :as routes]))

(e/defn NewGame []
  (e/client
    (let [!game-code (atom "")]
      (dom/div (dom/props {:class "h-full flex flex-col justify-center items-center gap-4"})
               (dom/label (dom/props {:class "flex gap-2"})
                 (dom/text "Enter Game Code:")
                          (dom/input
                            (dom/props {:placeholder "" :maxlength 100 :class "text-black"})
                            (dom/on "keyup" (e/fn [e]
                                                (when (= "Enter" (.-key e))
                                                  (when-some [v (empty->nil (.substr (.. e -target -value) 0 100))]
                                                    (routes/Navigate. :game {:path-params {:code v}})))
                                                (reset! !game-code (.-value dom/node))))))
               (Button. {:text "Join or Create Game"
                 :on-click (e/fn [] (routes/Navigate. :game {:path-params {:code @!game-code}}))})))))