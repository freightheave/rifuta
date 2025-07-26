(ns rifuta
  (:require
   [replicant.dom :as r]))

(defonce store (atom nil))

(defn exercise-input [e]
  (let [x (.-value (.-target e))]
    (swap! store (fn [state]
                   (assoc-in state [:current-set :exercise] x)))))

(defn weight-input [e]
  (let [x (.-valueAsNumber (.-target e))]
    (swap! store (fn [state]
                   (assoc-in state [:current-set :weight] x)))))

(defn note-input [e]
  (let [x (.-value (.-target e))]
    (swap! store (fn [state]
                   (assoc-in state [:current-set :note] x)))))

(defn reps-input [e]
  (let [x (.-valueAsNumber (.-target e))]
    (swap! store (fn [state]
                   (assoc-in state [:current-set :reps] x)))))

(defn store-set [] ; --submits to the atom, call button logic here.
  (swap! store (fn [state]
                 (let [curr (get state :current-set)
                       all-sets (get state :all-sets)]
                   (assoc state :all-sets (conj all-sets curr))))))

(def handler-by-name {:exercise-input exercise-input
                      :weight-input weight-input
                      :note-input note-input
                      :reps-input reps-input
                      :store-set store-set})

(defn render-logset-form [state] ; should return the HTML only. nope can handle everything here.
  [:div.all
   [:div.exercise "Exercise Name: " [:input {:type "text"
                                             :on {:input [:exercise-input]}}]] ; add listeners to all the divs, :on :input.Replace test with value from last set of exercise. Also, look into "Placeholder" attribute for pre-fill.
   [:div.weight "Weight: " [:input {:type "number"
                                    :on {:input [:weight-input]}}]]

   [:div.note "Note: " [:input {:type "text"
                                :on {:input [:note-input]}}]]
   [:div.reps "Repetitions: " [:input {:type "number"
                                       :on {:input [:reps-input]}}]]
   [:div.submit [:button {:on
                          {:click [:store-set]}}
                 "Submit"]]])

(defn render-done-sets [state]
  [:div [:p (str (peek (get state :all-sets)))]])

(defn render-app [state]
  [:div
   (render-logset-form state)
   (render-done-sets state)])

(defn main []
  (let [el (js/document.getElementById "app")]
    (r/set-dispatch!
      (fn [{e :replicant/dom-event :as event-data} [function-name & args :as handler-data]]
        (when (= :replicant.trigger/dom-event (:replicant/trigger event-data))
          (apply (handler-by-name function-name)
                 e args))))
    (add-watch store :watcher (fn [_ _ _ state]
                                (r/render el (render-app state))))
    (reset! store {:current-set {}, :all-sets []}))) ; always 'CALL' the render function.

; --- Testing code from here

(comment
  (def state {:current-set {:exercise "blah", :reps 200, :weight 2000, :note "ezpz"}, :all-sets [{:exercise "squats", :reps 200, :weight 2000, :note "ezpz"}]})
  (def state @store)
  (def x "lmao")
  (assoc-in state [:current-set :note] x)
  (assoc-in {} "lmao" 2))
