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
      (dom/div (dom/props {:class "flex flex-col items-center justify-center h-full gap-8"} )
        (dom/div (dom/props {:class "flex flex-col justify-center items-center gap-4"})
                 (dom/label (dom/props {:class "flex gap-2"})
                            (dom/text "Enter Game Code:")
                            (dom/input
                              (dom/props {:placeholder "" :maxlength 100 :class "text-black"})
                              (dom/on "keyup" (e/fn [e]
                                                (when (= "Enter" (.-key e))
                                                  (when-some [v (empty->nil (.substr (.. e -target -value) 0 100))]
                                                    (routes/Navigate. :game {:path-params {:code v}})))
                                                (reset! !game-code (.-value dom/node))))))
                 (Button. {:text     "Join or Create Game"
                           :on-click (e/fn [] (routes/Navigate. :game {:path-params {:code @!game-code}}))}))
        (dom/article
          (dom/props {:class "prose lg:prose-base dark:prose-invert p-4"})
          (dom/h2  (dom/text "Rules"))
          (dom/p (dom/text "The goal of the game is to play all of your cards and become the Cedar King."))
          (dom/p (dom/text "The starting player plays 1 or more cards of the same rank (1-12).
        Future players must play the same number of cards, with a matching or lower rank (ex. 11 may be played on top of 12, but not on top of 10).
        Each rank has as many cards as its value in the deck (i.e. there is one '1', two '2's, etc.)"))
          (dom/p (dom/text "Once a player plays all of their cards they become the \"Cedar King\". Play continues until all players except one have played all their cards (this player is denoted the \"Weed\").
        When a new round starts the Weed must give the Cedar King their two best cards, and the Cedar King returns two cards of their choice to the Weed.
        Similarly the second place player (the Pine Prince) receives one card from the second to last player (the Bush Pauper) and gives one card of their choice back."))
          (dom/p (dom/text "Them's the rules. Have fun and remember that life isn't fair!")))))))