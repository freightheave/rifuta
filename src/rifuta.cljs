(ns rifuta
  (:require
   [cljs.reader]
   [replicant.dom :as r]
   [rifuta.opfs :as opfs]))

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

(defn store-set [_e]
  (let [new-state (swap! store (fn [state]
                                 (let [curr (get state :current-set)
                                       all-sets (get state :all-sets)]
                                   (-> state
                                     (assoc :all-sets (conj all-sets curr))
                                     (update :current-set dissoc :note)))))]
    (-> (opfs/write "store.edn", (pr-str new-state))
      (.catch (fn [_]
                (js/localStorage.setItem "store.edn", (pr-str new-state)))))))

(defn download-as-file
  "Downloads a blob, creates temp element and releases ram from blob after DL."
  [content file-name]
  (let [url (->> {:type "text/plain"}
                 clj->js
                 (js/Blob. [content])
                 (js/URL.createObjectURL))
        link (js/document.createElement "a")]
    (set! (.-href link) url)
    (set! (.-download link) file-name)
    (.click link)
    (js/URL.revokeObjectURL url)))    ;; release ram from the blob

(defn export-all-sets [_e]
  (download-as-file @store "rifuta-sets-export.txt")) ;; just a lilbit redundant

(def handler-by-name {:exercise-input exercise-input
                      :weight-input weight-input
                      :note-input note-input
                      :reps-input reps-input
                      :store-set store-set
                      :export-all-sets export-all-sets})

(defn today []
  (str (first (.split (.toISOString (js/Date.))"T")), ".txt"))

(defn render-logset-form [{{:keys [exercise note weight reps]} :current-set :as _state}]
  [:div.all
   [:div.exercise "Exercise Name: " [:input {:type "text"
                                             :value exercise
                                             :on {:input [:exercise-input]}}]]
   [:div.weight "Weight: " [:input {:type "number"
                                    :value weight
                                    :on {:input [:weight-input]}}]]
   [:div.note "Note: " [:input {:type "text"
                                :value note
                                :on {:input [:note-input]}}]]
   [:div.reps "Repetitions: " [:input {:type "number"
                                             :value reps
                                             :on {:input [:reps-input]}}]]
   [:div.submit [:button {:on
                          {:click [:store-set]}}
                 "Submit2"]]])

(defn render-done-sets [state]
  [:div [:p (str (peek (get state :all-sets)))]])

(defn render-app [state]
  [:div
   (render-logset-form state)
   (render-done-sets state)
   [:div [:button {:on
                   {:click [:export-all-sets]}}
          "Export All Sets"]]])

(defn main []
  (-> (js/navigator.serviceWorker.register "/sw.js" (clj->js {:scope "/"}))
      (.then #(js/console.log %))
      (.catch #(js/console.log % "SW Reg Failed.")))
  (let [el (js/document.getElementById "app")]
    (r/set-dispatch!
      (fn [{e :replicant/dom-event :as event-data} [function-name & args :as _handler-data]]
        (try
          (when (= :replicant.trigger/dom-event (:replicant/trigger event-data))
            (apply (handler-by-name function-name)
                   e args))
          (catch js/Error err
            (js/console.log "Event Handler failed" err)))))
    (add-watch store :watcher (fn [_ _ _ state]
                                (r/render el (render-app state))))
    (-> (opfs/read "store.edn")
        (.then (fn [store-str]
                 (reset! store (cljs.reader/read-string store-str))))
        (.catch (fn [_]
                  (let [store-str (js/localStorage.getItem "store.edn")]
                    (if (nil? store-str)
                      (reset! store {:current-set {}, :all-sets []})
                      (reset! store (cljs.reader/read-string store-str)))))))))

;; --- TEST CODE

(comment
  (def state {:current-set {:exercise "blah", :reps 200, :weight 2000, :note "ezpz"}, :all-sets [{:exercise "squats", :reps 200, :weight 2000, :note "ezpz"}]})
  (str state)
  (def state @store)
  (def x "lmao")
  (assoc-in state [:current-set :note] x)
  (assoc-in {} "lmao" 2)
  (str (js/Date.now), ".txt")
  (str (.split (.toISOString (js/Date.)) 'T'), ".txt")
  (let [x (first (.split  (.toISOString (js/Date.))"T"))]
    x)
  (first (cljs.core/js->clj (.split (.toISOString (js/Date.))"T")))
  (js/Date.now)
  (+ 1 3) ; 4
  (.catch (.then (opfs/read "foo.txt")
            #(def s2 %))
          #(def error1 %))
  s2
  (js/console.log error1)
  (let [x (js/localStorage.getItem "st")]
    (if (nil? x)
      (reset! store {:current-set {}, :all-sets []})))
  (throw (js/Error. "Oops"))
  
  ,)

; ---Connect to browser REPL
(comment
  (js/alert "foo")
  ;; browser-REPL
  :cljs/quit
  (do
    (clojure.core/require 'shadow.cljs.devtools.api)
    (shadow.cljs.devtools.api/repl :app))
  
  ,)
