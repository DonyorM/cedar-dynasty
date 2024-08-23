(ns cedar-dynasty.scenes.login
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [cedar-dynasty.components.button :refer [Button]]))

(e/defn Login []
  (e/client
    (dom/div (dom/props {:class "h-full text-center flex flex-col justify-center items-center gap-2 p-4"})
             (dom/h1 (dom/props {:class "text-3xl font-bold"}) (dom/text "Cedar Dynasty"))
             (dom/p (dom/text "Please click below to login"))
             (dom/a (dom/props {:href "/oauth2" :class "rounded-lg bg-cyan-600 hover:bg-cyan-400 py-2 px-4 font-bold disabled:bg-gray-400 disabled:text-gray-800"}) (dom/text "Sign In")))))