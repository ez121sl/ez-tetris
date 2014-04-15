(defproject ez-tetris "0.1.0-SNAPSHOT"
  :description "Tetris-like game for Maya."
  :url "http://54.201.123.111/ez-tetris"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                 [om "0.5.0"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "ez-tetris"
              :source-paths ["src"]
              :compiler {
                :output-to "ez_tetris.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
