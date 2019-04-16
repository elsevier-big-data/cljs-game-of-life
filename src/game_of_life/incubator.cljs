(ns game-of-life.incubator
  (:require [game-of-life.state :refer [state update-state!]]
            [reagent.core :as reagent]
            [antizer.reagent :as ant]))

(def width 50)
(def height 50)
(def block-size 10)

;; Logic
;; --------------------------------------------------
(defn get-neighbours
  [[x y]]
  [[(dec x) (dec y)] [x (dec y)] [(inc x) (dec y)]
   [(dec x) y] [(inc x) y]
   [(dec x) (inc y)] [x (inc y)] [(inc x) (inc y)]])


(defn is-alive?
  [grid coord]
  (let [pop-count (count (select-keys grid (get-neighbours coord)))
        is-alive (contains? grid coord)]
    (cond
      (and is-alive (< pop-count 2)) false
      (and is-alive (<= 2 pop-count 3)) true
      (and is-alive (> pop-count 3)) false
      (and (not is-alive) (= pop-count 3)) true
      :else false)))

(defn logic
  [grid]
  (into {} (for [x (range 0 width)
                 y (range 0 height)
                 :when (is-alive? grid [x y])]
             [[x y] :live]))
  )

(defn tick []
  (update-state! #(-> %
                      (update-in [:time] inc)
                      (update-in [:grid] logic))))

(defn- recurring-timer [f]

  (letfn [(trigger-and-renew []
            (let [period (:delay @state)]
              (do
                (f)
                (js/setTimeout trigger-and-renew period))))]
    (js/setTimeout trigger-and-renew 0)))

(defonce tick-timer
         (recurring-timer tick))


;; View
;; --------------------------------------------------

(def background-color "#0D0208")
(def block-color "#008F41")

(defn- draw-cell [ctx [x y]]
  (.fillRect ctx (* x block-size) (* y block-size) block-size block-size))

(defn- draw-grid [canvas]
  (let [ctx (.getContext canvas "2d")]
    (set! (.-fillStyle ctx) background-color)
    (.fillRect ctx 0 0 (* width block-size) (* height block-size))
    (set! (.-fillStyle ctx) block-color)
    (dorun (map (partial draw-cell ctx) (keys (:grid @state))))))

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

(defn- increment-delay
  []
  (update-state! #(-> %
                      (update-in [:delay] (partial + 100)))))
(defn- decrement-delay
  []
  (update-state! #(-> %
                      (update-in [:delay] (fn [x] (- x 100))))))

(defn view [params]
  [:div {:style {:padding "5px"}}Ò
   [:h1 "Game of Life"]
   [:div {:style {:padding "5px"}}
    [:p (str "Generation: " (:time @state))]
    [:p (str "Delay: " (:delay @state))]
    [ant/form {:layout "inline"}
     [ant/form-item {}
      [ant/button {:type "primary" :on-click increment-delay} "Slower"]]
     [ant/form-item {}
      [ant/button {:type "primary" :on-click decrement-delay} "Faster"]]]
    [div-with-canvas {:width  (* width block-size)
                      :height (* height block-size)} draw-grid]]])
