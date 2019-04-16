(ns game-of-life.state
  (:require
   [reagent.core :as reagent]))


(def width 50)
(def height 50)

(defn random-grid []
  (let [random-coords (repeatedly #(vec [(rand-int width) (rand-int height)]))]
    (into {} (for [k (take (rand-int (* width height)) random-coords)]
       [k :live]))))

;; Application state
(defonce state (reagent/atom {:time 0
                              :grid (random-grid)
                              :delay 500}))

(defn update-state! [f & args]
  (swap! state f)
  (js/console.info @state))

