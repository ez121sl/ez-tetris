(ns ez-tetris.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [ez-tetris.util :refer [beat]]
            [ez-tetris.tetris :refer [play]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! map> filter>]]
            [goog.events :as events]
            [goog.Timer])
  (:import goog.Timer
           goog.events.KeyHandler))

(enable-console-print!)

(.addEventListener js/window "load" #(.attach js/FastClick (.-body js/document)) false)

(defn initial-state [] { :game (play nil :new)
                         :name "EZ Tetris"})

(def game (atom (initial-state)))

(defn- click-handler [chan cmd]
  (fn [evt]
    (.blur (.-target evt))
    (put! chan cmd)))

(defn- render-field [field & [classes]]
  (apply dom/div #js { :className (:field classes "field") }
         (for [row field]
           (apply dom/div #js { :className (:row classes "row") }
                  (for [cell row]
                    (dom/span #js { :className (str (:cell classes "cell") " " (name cell)) } "\u00A0"))))))

(defn field [game owner]
  (reify
    om/IRender
    (render [_]
            (render-field (:view game)))))

(defn controls [app owner]
  (reify
    om/IRenderState
    (render-state [_ { :keys [command] }]
            (dom/div #js { :className "controls" }
                     (dom/button #js { :className "thumb" :onClick (click-handler command :left) } "left")
                     (dom/button #js { :className "thumb spaced" :onClick (click-handler command :rotate) } "rotate")
                     (dom/button #js { :className "thumb" :onClick (click-handler command :right) } "right")
                     (dom/button #js { :className "thumb" :onClick (click-handler command :down) } "down")))))

(defn misc-info [game owner]
  (reify
    om/IRender
    (render [_]
            (dom/div #js {:className "misc-info"}
                     (dom/div #js {:className "misc-row"} "Up next:")
                     (render-field (:view-next game) { :field "misc-row" })
                     (dom/div #js {:className "misc-row"} "Score:")
                     (dom/div #js {:className "misc-row"} (:score game))))))

(defn game-controls [app owner]
  (reify
    om/IRenderState
    (render-state [_ { :keys [command] }]
            (dom/div #js { :className "game-controls" }
                     (dom/button #js { :onClick (click-handler command :new) } "New game")))))

(comment

  TODO:

  - Different levels + start / stop / pause game
  + view next next shape
  + score
  + push to github
  + deploy to Amazon
  + optimize for mobile

  )

(def commands #{ :left :right :down :rotate :new })

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
                      key-map { 32 :rotate, 37 :left, 39 :right, 40 :down }
                      key-chan (map> #(key-map (.-keyCode %) :none) command)]
                  (events/listen timer Timer/TICK (fn [_] (put! command :down)))
                  (events/listen key-handler "key" (fn [e] (put! key-chan e)))
                  (go (loop []
                        (let [cmd (<! command)]
                          ;; (println "command " key)
                          (om/transact! app :game #(play % cmd))
                          (recur))))))
    om/IWillUpdate
    (will-update [_ next-props next-state]
                 ;; (println next-props " - " next-state)
                 (when (and (get-in next-props [:game :lost]) (not (get-in app [:game :lost])))
                   (.stop (:timer next-state)))
                 (when (and (not (get-in next-props [:game :lost])) (get-in app [:game :lost]))
                   (beat (:timer next-state) 500)))
    om/IRenderState
    (render-state [_ state]
                  (dom/div nil
                           (dom/h1 nil (:name app))
                           (om/build game-controls nil { :init-state state })
                           (om/build field (:game app))
                           (om/build misc-info (:game app))
                           (if (get-in app [:game :lost])
                             (dom/h2 nil "Game over!")
                             (om/build controls nil { :init-state state }))))))

(om/root
  tetris
  game
  {:target (. js/document (getElementById "app"))})
