(ns test
  (:require [replicant.dom :as r]))

(def a (atom nil))

(defn render-inc-test [m]
  [:div#counter
   [:input#inputbox {:type "number",
                     :value (:incrementor m),
                     :on {:input (fn [e]
                                   (let [x (.-valueAsNumber (.-target e))]
                                     (swap! a (fn [m]
                                                (assoc m :incrementor x)))))}}] ;(swap! a fn [m] (assoc m :incrementor x)) == (apply f '(current value of a) '(no args here)) -> this means that f is being called with current value of a (which is the map.)
   [:div#displaythething (:incremented m)]
   [:button {:on {:click
                  (fn [e]
                    (swap! a (fn [m]
                               (assoc m :incremented
                                      (+ (:incremented m) (:incrementor m))))))}}

    "Click Me"]])

(defn main []
  (let [el (js/document.getElementById "app")]
    (add-watch a :watcher (fn [_ _ _ new-state]
                            (r/render el (render-inc-test new-state))))
    (reset! a {:incrementor 1, :incremented 0})))



