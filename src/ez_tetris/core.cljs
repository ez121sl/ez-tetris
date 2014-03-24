(ns ez-tetris.core
  (:require [ez-tetris.util :refer [vec2d]]
            [ez-tetris.tetris :refer [next-shape move rotate left right down]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def initial-state { :game { :field (vec2d 10 10 :blank)
                             :shape (next-shape)
                             :pos [0 5] }
                     :text "Hello ez-tetris!" })

(def game (atom initial-state))

(defn tetris [app owner]
  (reify
    om/IRender
    (render [_ ]
            (dom/h1 nil (:text app)))))

(om/root
  tetris
  game
  {:target (. js/document (getElementById "app"))})
