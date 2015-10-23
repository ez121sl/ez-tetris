(defproject ez-tetris "0.1.0-SNAPSHOT"
  :description "Tetris-like game for Maya."
  :url "https://github.com/ez121sl/ez-tetris"

  :license { :name "Eclipse Public License - v 1.0"
             :url "http://www.eclipse.org/legal/epl-v10.html"
             :distribution :manual
             :comments "same as Clojure"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145" :classifier "aot"
                  :exclusion [org.clojure/data.json]]
                 [org.clojure/data.json "0.2.6" :classifier "aot"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.omcljs/om "0.8.8"]]

  :plugins [[lein-cljsbuild "1.1.0"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "ez-tetris"
              :source-paths ["src"]
              :compiler {
                :main ez-tetris.core
                :output-to "ez_tetris.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}
             {:id "release"
              :source-paths ["src"]
              :compiler {
                :main ez-tetris.core
                :output-to "ez_tetris_release.js"
                :optimizations :advanced
                :pretty-print false}}]})
