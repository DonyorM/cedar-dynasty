(ns great-dalmuti.login
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-ui4 :as ui4]
            [hyperfiddle.electric-dom2 :as dom]
            [great-dalmuti.components.button :refer [Button]]))

(def logged-in-users (atom {}))
(e/defn Login [set-current-user]
  (e/client
    (let [!username (atom "")
          username (e/watch !username)
          a (e/watch logged-in-users)]
      (ui4/input username (e/fn [v] (reset! !username v))
                 (dom/props {:class "text-black"}))
      (Button.
        {:on-click
         (e/fn []
           (e/client
             (if-let [current-user-id (get @logged-in-users username)]
               (new set-current-user current-user-id username)
               (let [new-user-id (random-uuid)]
                 (swap! logged-in-users assoc username new-user-id)
                 (new set-current-user new-user-id username)))))
         :text "Submit"})
      (dom/p (dom/text a)))))