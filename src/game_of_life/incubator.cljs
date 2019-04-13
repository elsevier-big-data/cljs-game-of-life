(ns game-of-life.incubator
  (:require [game-of-life.state :refer [state update-state!]]
            [reagent.core :as reagent]))

(def width 50)
(def height 50)
(def block-size 10)

(defn random-grid [grid]
  (let [random-coords (repeatedly #(vec [(rand-int width) (rand-int height)]))]
    (into #{} (take (rand-int (* width height)) random-coords))))

(defn tick []
  (update-state! #(-> %
                      (update-in [:time] inc)
                      (update-in [:grid] random-grid))))

(defn- recurring-timer [f period]
  (letfn [(trigger-and-renew [] (do
                                  (f)
                                  (js/setTimeout trigger-and-renew period)))]
    (js/setTimeout trigger-and-renew period)))

(defonce tick-timer
  (recurring-timer tick 500))

;; View
;; --------------------------------------------------

(def background-color "#0D0208")
(def block-color      "#008F41")

(defn- draw-cell [ctx [x y]]
  (.fillRect ctx (* x block-size) (* y block-size) block-size block-size))

(defn- draw-grid [canvas]
  (let [ctx (.getContext canvas "2d")]
    (set! (.-fillStyle ctx) background-color)
    (.fillRect ctx 0 0 (* width block-size) (* height block-size))
    (set! (.-fillStyle ctx) block-color)
    (dorun (map (partial draw-cell ctx) (:grid @state)))))

(defn- div-with-canvas
  "A canvas within a :div, which will render (f-draw context)."
  [properties f-draw]
  (let [dom-node (reagent/atom nil)]
    (reagent/create-class
     {:display-name "div-with-canvas"

      :component-did-mount
      (fn [this]
        (reset! dom-node (reagent/dom-node this)))

      ;; Arguments must match the outer arguments. Also, because reagent-render is called prior to mounting, we need to
      ;; protect against the null dom-node on the first render
      :reagent-render
      (fn [properties f-draw]
        (if-let [node @dom-node] (f-draw (.-firstChild node)))
        [:div [:canvas properties]])})))

(defn view [params]
  [:div {:style {:padding "5px"}}
   [:h1 "Game of Life"]
   [:div {:style {:padding "5px"}}
    [:p (str "Generation: " (:time @state))]
    [div-with-canvas {:width  (* width block-size)
                      :height (* height block-size)} draw-grid]]])
