(ns cedar-dynasty.components.hand-wheel
  (:require [cedar-dynasty.spec :as spec]
            [cedar-dynasty.components.card :refer [Card]]
            [hyperfiddle.electric-svg :as svg]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [cedar-dynasty.utils :as u]))

(def sensitivity 10)
(e/defn HandWheel [cards {:keys [selected on-select]}]
  (e/client
    (let [!offset (atom 0)
          offset (e/watch !offset)
          swipe-start-val (atom nil)
          type-counts (count cards)]
      ;; The padding on the bottom means the transformed cards won't disappear down the bottom of the page
      (dom/div (dom/props {:class "overflow-x-scroll overflow-y-hidden pb-20"})
               (dom/div (dom/props {:class "flex px-4 transition-transform"
                                    :style {:transform (str "translateX(-" (* offset 7) "rem)")}})
                        (dom/on! "touchstart" (fn [event]
                                                  (reset! swipe-start-val
                                                          (.-screenX (first (.-changedTouches event))))))
                        (dom/on! "touchend"
                                   (fn [event]
                                     (let [finalX (.-screenX (first (.-changedTouches event)))
                                           curr-swipe-start-val @swipe-start-val
                                           diff (abs (- finalX curr-swipe-start-val))]
                                       (if (> diff sensitivity)
                                         (if (< finalX curr-swipe-start-val)
                                           (when (< offset (dec type-counts))
                                             (swap! !offset inc))
                                           (when (> offset 0)
                                             (swap! !offset dec)))))))
                        (e/server
                          (e/for-by key [[card-val card-count] (sort-by #(.indexOf spec/card-order (first %))
                                                                        cards)]
                                    (when (> card-count 0)
                                      (e/client
                                        (Card. card-val card-count
                                               {:selected (= selected card-val)
                                                :on-click on-select}))))))))))