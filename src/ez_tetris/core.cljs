(ns ez-tetris.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [ez-tetris.util :refer [beat]]
            [ez-tetris.tetris :refer [next-shape move rotate left right down render starting-state]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! map> filter>]]
            [goog.events :as events]
            [goog.Timer])
  (:import goog.Timer
           goog.events.KeyHandler))

(enable-console-print!)

(defn initial-state [] { :game (starting-state)
                         :name "ez-tetris"})

(def game (atom (initial-state)))

(def commands #{ :left :right :down :rotate })

(defmulti handler (fn [cmd _] cmd))

(defmethod handler :left [_ game]
  (move game left))

(defmethod handler :right [_ game]
  (move game right))

(defmethod handler :down [_ game]
  (move game down))

(defmethod handler :rotate [_ game]
  (rotate game))

(defmethod handler :default [_ game]
  game)

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
                { :command (filter> commands (chan))
                  :timer (Timer.)
                  :key-handler (KeyHandler. js/document)})
    om/IWillMount
    (will-mount [_]
                (let [command (om/get-state owner :command)
                      timer (beat (om/get-state owner :timer) 500)
                      key-handler (om/get-state owner :key-handler)
                      key-map { 32 :rotate, 37 :left, 39 :right, 40 :south }
                      key-chan (map> #(key-map (.-keyCode %) :none) command)]
                  (events/listen timer Timer/TICK (fn [_] (put! command :down)))
                  (events/listen key-handler "key" (fn [e] (put! key-chan e)))
                  (go (loop []
                        (let [cmd (<! command)]
                          ;; (println "command " key)
                          (om/transact! app :game #(handler cmd %))
                          (recur))))))
    om/IWillUpdate
    (will-update [_ next-props next-state]
                 ;; (println next-props " - " next-state)
                 (when (get-in next-props [:game :lost])
                   (.stop (:timer next-state))))
    om/IRenderState
    (render-state [_ state]
            (dom/div nil
                     (dom/h1 nil "EZ Tetris")
                     (om/build field app)
                     (if (get-in app [:game :lost])
                       (dom/h2 nil "Game over!")
                       (om/build controls app { :init-state state }))))))

(om/root
  tetris
  game
  {:target (. js/document (getElementById "app"))})
