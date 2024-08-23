(ns cedar-dynasty.scenes.collect-user-name
  (:require [hyperfiddle.electric :as e]
            [cedar-dynasty.components.button :refer [Button]]
            [contrib.str :refer [empty->nil]]
            [hyperfiddle.electric-dom2 :as dom]
            #?(:clj [cedar-dynasty.backend.db :as db])))


(e/defn CollectUserName [user-id on-name-set]
  (e/server
    (let []
      (e/client
        (let [!name (atom "")
              save-name (e/fn [name]
                          (e/server (db/set-user-name user-id name)
                                    (on-name-set name)))]
          (dom/div (dom/props {:class "h-full flex flex-col justify-center items-center gap-4"})
                   (dom/label (dom/props {:class "flex gap-2"})
                              (dom/text "Enter Your Display Name:")
                              (dom/input
                                (dom/props {:placeholder "" :maxlength 100 :class "text-black"})
                                (dom/on "keyup" (e/fn [e]
                                                    (when (= "Enter" (.-key e))
                                                      (when-some [v (empty->nil (.substr (.. e -target -value) 0 100))]
                                                        (new save-name v)))
                                                    (reset! !name (.-value dom/node))))))
                   (Button. {:text     "Set Name"
                             :on-click (e/fn [] (new save-name @!name))})))))))
