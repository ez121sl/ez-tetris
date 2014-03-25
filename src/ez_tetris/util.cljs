(ns ez-tetris.util)

(defn vec2d [w h val]
   (vec (repeat h (vec (repeat w val)))))

(defn beat [timer timeout]
  (doto timer
    (.stop)
    (.setInterval timeout)
    (.start)))

