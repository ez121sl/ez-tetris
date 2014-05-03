(ns ez-tetris.tetris
  (:require [ez-tetris.util :refer [vec2d]]))

(def ^:private width (comp count first))
(def ^:private height count)

(def shapes [[[:red :red :blank]
              [:blank :red :red]],
             [[:blank :green :green]
              [:green :green :blank]],
             [[:blue :blue :blue :blue]],
             [[:yellow :yellow]
              [:yellow :yellow]]
             [[:brown :brown :brown]
              [:brown :blank  :blank]]
             [[:orange :orange :orange]
              [:blank :blank :orange]]])

(defn- rotate-shape [s]
  (apply mapv vector (map reverse s)))

(defn- overlayed-val [field-val shape-val]
  (if (= field-val :blank)
    shape-val
    (when (= shape-val :blank)
      field-val)))

(defn- all-coords [shape]
  (for [yy (range (width shape)), xx (range (height shape))]
    [xx yy]))

(defn- overlay [field shape [x y]]
  (reduce (fn [field [xx yy]]
              (update-in field
                         [(+ xx x) (+ yy y)]
                         overlayed-val
                         (get-in shape [xx yy])))
          field
          (all-coords shape)))

(defn- allowed? [overlayed-field]
  (not-any? nil? (apply concat overlayed-field)))

(defn- contained? [field shape [row col]]
  (and (<= 0 row (+ row (height shape)) (height field))
       (<= 0 col (+ col (width shape)) (width field))))

(defn- overlay-allowed [field shape pos]
  (when (contained? field shape pos)
    (let [overlayed-field (overlay field shape pos)]
      (when (allowed? overlayed-field)
        overlayed-field))))

(defn- full-rows [field]
  (reduce-kv #(if (not-any? #{:blank} %3) (conj %1 %2) %1) [] field))

(defn- remove-rows [field rows-to-remove]
  (replace field  (filterv (complement (set rows-to-remove)) (range (count field)))))

(defn- prepend-rows [field num-rows]
  (vec (concat (vec2d (width field) num-rows :blank) field)))

(defn- next-pos [pos dir]
  (mapv + pos dir))

(defn- start-pos [field shape]
  [0 (quot (- (width field) (width shape)) 2)])

(defn- next-shape []
  (nth shapes (rand-int (count shapes))))

(defn starting-state []
  (let [shape (next-shape)
        shape2 (next-shape)
        field (vec2d 10 16 :blank)
        pos (start-pos field shape)]
    { :field field
      :shape shape
      :shape-next shape2
      :pos pos
      :score 0
      :view (overlay-allowed field shape pos)
      :view-next (overlay-allowed (vec2d 4 2 :blank) shape2 [0 0]) }))

(defn rotate [{:keys [field shape pos] :as game}]
  (let [rotated (rotate-shape shape)]
    (if-let [view (overlay-allowed field rotated pos)]
      (merge game { :shape rotated :view view} )
      game)))

(def left [0 -1])
(def right [0 1])
(def down [1 0])

(defn move [{:keys [field shape pos shape-next score] :as game} requested-dir]
  (let [new-pos (next-pos pos requested-dir)]
    (if-let [overlayed (overlay-allowed field shape new-pos)]
      (merge game { :pos new-pos :view overlayed } )
      (if-not (= requested-dir down)
        game
        (let [new-field (overlay-allowed field shape pos)
              rm-rows (full-rows new-field)
              new-field (-> new-field
                            (remove-rows rm-rows)
                            (prepend-rows (count rm-rows)))
              new-shape shape-next
              new-shape2 (next-shape)
              start-pos (start-pos new-field new-shape)
              game (-> game
                       (assoc :field new-field)
                       (assoc :shape new-shape)
                       (assoc :shape-next new-shape2)
                       (assoc :pos start-pos)
                       (assoc :score (+ score (* 10 (count rm-rows)))))]
          (if-let [view (overlay-allowed new-field new-shape start-pos)]
            (merge game { :view view :view-next (overlay-allowed (vec2d 4 2 :blank) new-shape2 [0 0])})
            (assoc game :lost true)))))))
