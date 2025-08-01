(ns test
  (:require
   [replicant.dom :as r]))

(def a (atom nil))

(defn download-button []
  (let [blob (js/Blob. [@a] (clj->js {:type "text/plain"}))
        link (js/document.createElement "a")
        lmao (js/URL.createObjectURL blob)]
    (set! (.-href link) lmao)
    (set! (.-download link) "download_file.txt")
    (.click link)
    ;; URL.revokeObjectURL(link.href);
    (js/URL.revokeObjectURL (.-href link))))

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
               :input-box-fn input-box-fn
               :download-button download-button}]
        (apply (m (first handler-data))
               e (rest handler-data))))
    (js/console.log event-data handler-data)))

(defn render-inc-test [m]
  [:div#counter
   [:input#inputbox {:type "number"
                     :value (inc (:incrementor m))
                     :on {:input [:input-box-fn]}}] ; (swap! a fn [m] (assoc m :incrementor x)) == (apply f '(current value of a) '(no args here)) -> this means that f is being called with current value of a (which is the map.)

   [:div#displaythething (:incremented m)]
   [:button {:on {:click [:button-click-fn 1]}}

    "Click Me"]
   [:button {:on {:click [:download-button]}}
    "Download"]])

(defn main []
  (let [el (js/document.getElementById "app")]
    (add-watch a :watcher (fn [_ _ _ new-state]
                            (r/render el (render-inc-test new-state))))
    (reset! a {:incrementor 1, :incremented 0})))

;;; TEST

(comment
  (js/alert "foo")
  ;; browser-REPL
  :cljs/quit
  (do
    (clojure.core/require 'shadow.cljs.devtools.api)
    (shadow.cljs.devtools.api/repl :test))
  (swap! a assoc :incrementor 2)
  
  
  ,)
