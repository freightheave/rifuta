(ns rifuta
  (:require [replicant.dom :as r]))

(defonce store (atom nil))

(defn render-logset-form [state] ;should return the HTML only. nope can handle everything here.
  [:div.all
   [:div.exercise "Exercise Name: " [:input {:type "text",
                                             :on {:input (fn [e]
                                                           (let [x (.-value (.-target e))]
                                                             (swap! store (fn [state]
                                                                            (assoc-in state [:current-set :exercise] x)))))}}]] ; add listeners to all the divs, :on :input.Replace test with value from last set of exercise. Also, look into "Placeholder" attribute for pre-fill.
   [:div.weight "Weight: " [:input {:type "number",
                                    :on {:input (fn [e]
                                                  (let [x (.-valueAsNumber (.-target e))]
                                                    (swap! store (fn [state]
                                                                   (assoc-in state [:current-set :weight] x)))))}}]]

   [:div.note "Note: " [:input {:type "text",
                                :on {:input (fn [e]
                                              (let [x (.-value (.-target e))]
                                                (swap! store (fn [state]
                                                               (assoc-in state [:current-set :note] x)))))}}]]
   [:div.reps "Repetitions: " [:input {:type "number",
                                       :on {:input (fn [e]
                                                     (let [x (.-valueAsNumber (.-target e))]
                                                       (swap! store (fn [state]
                                                                      (assoc-in state [:current-set :reps] x)))))}}]]
   [:div.submit [:button {:on
                          {:click (fn [state] ;--submits to the atom, call button logic here.
                                    (swap! store (fn [state]
                                                   (let [curr (get state :current-set),
                                                         all-sets (get state :all-sets)]
                                                     (assoc state :all-sets (conj all-sets curr))))))}}
                 "Submit"]]])

(defn render-done-sets [state]
  [:div [:p (str (peek (get state :all-sets)))]])

(defn render-app [state]
  [:div
   (render-logset-form state)
   (render-done-sets state)])

;Show the previous set. Can't render with the same function, so look into if it can be done with mulitple render functions or need to tweak the index.html to include more divs. why not? just stick another div in index.html and call another render func.
(defn render-prevset-form [& args])

(defn button-logic [& args]
  ;something related to button goes here. (not really, can be handled in the button div fn only.)
  )

(defn main []
  (let [el (js/document.getElementById "app")]
    (add-watch store :watcher (fn [_ _ _ state]
                                (r/render el (render-app state))))
    (reset! store {:current-set {}, :all-sets []}))) ;always 'CALL' the render function.

; --- Testing code from here

(comment
  (def state {:current-set {:exercise "blah", :reps 200, :weight 2000, :note "ezpz"}, :all-sets [{:exercise "squats", :reps 200, :weight 2000, :note "ezpz"}]})
  (def state @store)
  (def x "lmao")
  (assoc-in state [:current-set :note] x)
  (assoc-in {} "lmao" 2))
