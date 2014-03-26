(ns ez-tetris.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [ez-tetris.util :refer [vec2d beat]]
            [ez-tetris.tetris :refer [next-shape move rotate left right down render]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [goog.events :as events]
            [goog.Timer])
  (:import goog.Timer))

(enable-console-print!)

(def initial-state { :game { :field (vec2d 10 10 :blank)
                             :shape (next-shape)
                             :pos [0 5] }
                     :name "ez-tetris"})

(def game (atom initial-state))

(defmulti handler (fn [cmd _] cmd))

(defmethod handler :left [_ game]
  (move game left))

(defmethod handler :right [_ game]
  (move game right))

(defmethod handler :down [_ game]
  (move game down))

(defmethod handler :rotate [_ game]
  (rotate game))

(defn field [app owner]
  (reify
    om/IRender
    (render [_]
            (apply dom/div #js { :className "field" }
                   (for [row (render (:game app))]
                     (apply dom/div #js { :className "row" }
                            (for [cell row]
                              (dom/span #js { :className (str "cell "(name cell)) } "\u00A0"))))))))

(defn controls [app owner]
  (reify
    om/IRenderState
    (render-state [_ { :keys [command] }]
            (dom/div nil
                     (dom/button #js { :onClick #(put! command :left) } "left")
                     (dom/button #js { :onClick #(put! command :rotate) } "rotate")
                     (dom/button #js { :onClick #(put! command :down) } "down")
                     (dom/button #js { :onClick #(put! command :right) } "right")))))

(defn tetris [app owner]
  (reify
    om/IInitState
    (init-state [_]
                { :command (chan)
                  :timer (Timer.)})
    om/IWillMount
    (will-mount [_]
                (let [command (om/get-state owner :command)
                      timer (beat (om/get-state owner :timer) 500)]
                  (events/listen timer Timer/TICK #(put! command :down))
                  (go (loop []
                        (let [key (<! command)]
                          (println "command")
                          (om/transact! app :game #(handler key %))
                          (recur))))))
    om/IRenderState
    (render-state [_ state]
            (dom/div nil
                     (dom/h1 nil "EZ Tetris")
                     (om/build field app)
                     (om/build controls app { :init-state state })))))

(om/root
  tetris
  game
  {:target (. js/document (getElementById "app"))})
