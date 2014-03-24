(ns ez-tetris.util)

(defn vec2d [w h val]
   (vec (repeat h (vec (repeat w val)))))
