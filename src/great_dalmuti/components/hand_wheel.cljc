(ns great-dalmuti.components.hand-wheel
  (:require [great-dalmuti.spec :as spec]
            [great-dalmuti.components.card :refer [Card]]
            [hyperfiddle.electric-svg :as svg]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [great-dalmuti.utils :as u]))

(def sensitivity 10)
(e/defn HandWheel [cards]
  (e/client
    (let [!offset (atom 0)
          offset (e/watch !offset)
          swipe-start-val (atom nil)
          type-counts (count cards)]
      (dom/div (dom/props {:class "overflow-x-hidden"})
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
                          (e/for-by key [[card-val card-count] cards]
                                    (e/client
                                      (Card. card-val card-count)))))))))