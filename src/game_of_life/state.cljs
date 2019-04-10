(ns game-of-life.state
  (:require
   [reagent.core :as reagent]))

;; Application state
(defonce state (reagent/atom {:time 0
                              :grid #{}}))

(defn update-state! [f & args]
  (swap! state f)
  (js/console.info @state))

