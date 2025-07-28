(ns test
  (:require
   [replicant.dom :as r]))

(def a (atom nil))

(defn button-click-fn [& args]
  (swap! a (fn [m]
             (assoc m :incremented
                    (+ (:incremented m) (:incrementor m))))))

(defn input-box-fn [e]
  (let [x (.-valueAsNumber (.-target e))]
    (js/console.log "global dispatch handler let for input box")
    (swap! a (fn [m]
               (assoc m :incrementor x)))))

(r/set-dispatch!
  (fn [event-data handler-data]
    (when (= :replicant.trigger/dom-event (:replicant/trigger event-data))
      (let [e (:replicant/dom-event event-data)
            m {:button-click-fn button-click-fn
               :input-box-fn input-box-fn}]
        (apply (m (first handler-data))
               e (rest handler-data))))
    (js/console.log event-data handler-data)))

(defn render-inc-test [m]
  [:div#counter
   [:input#inputbox {:type "number"
                     :value (:incrementor m)
                     :on {:input [:input-box-fn]}}] ; (swap! a fn [m] (assoc m :incrementor x)) == (apply f '(current value of a) '(no args here)) -> this means that f is being called with current value of a (which is the map.)

   [:div#displaythething (:incremented m)]
   [:button {:on {:click [:button-click-fn 1]}}

    "Click Me"]])

(defn main []
  (let [el (js/document.getElementById "app")]
    (add-watch a :watcher (fn [_ _ _ new-state]
                            (r/render el (render-inc-test new-state))))
    (reset! a {:incrementor 1, :incremented 0})))

;;; TEST

(comment
  (def handler-data "lol")
  (case (first handler-data)
    (\l) :test1
    (\o) :test2)
  (if false
    (prn "lkmao")
    (if true
      (prn)
      (prn "test")))

  (defn fac [n]
    (if (= n 0)
      1 (* n (fac (dec n)))))

  ,)
