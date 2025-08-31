(ns rifuta
  (:require
   [cljs.reader]
   [replicant.dom :as r]
   [rifuta.opfs :as opfs]))

(defonce store (atom nil)) ;; {:current-set {}, :all-sets [{}], :errors ()}

(defn conj-err [s]
  (swap! store update :errors conj s)
  (swap! store update :logs conj {:msg s})
  ,)

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
        (.then #(conj-err "wrote to opfs"))
        (.catch (fn [_]
                  (conj-err "Failed opfs/write")
                  (try
                    (js/localStorage.setItem "store.edn", (pr-str new-state))
                    (conj-err "Load to local storage.")
                    (catch js/Error err
                      (conj-err (str "Failed localStorage write", err)))))))))

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
                 "Submit"]]])

(defn render-done-sets [state]
  [:div [:p (str (peek (get state :all-sets)))]])

(defn render-app [state]
  [:div
   (render-logset-form state)
   (render-done-sets state)
   [:ol
    (for [x (:errors state)]
      [:li [:pre x]])]
   [:div [:button {:on
                   {:click [:export-all-sets]}}
          "Export All Sets"]]
   [:div.toast-container
    (for [log (:logs state)]
      [:div.toast [:pre (:msg log)]])]])

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
                 (reset! store (cljs.reader/read-string store-str))
                 (conj-err  (str "Read2 from opfs/read" store-str))))
        (.catch (fn [e]
                  (conj-err (str "Failed OPFS/Read" e))
                  (let [store-str (js/localStorage.getItem "store.edn")]
                    (if (nil? store-str)
                      (do
                        (swap! store merge {:current-set {}, :all-sets []})
                        (conj-err "Reset to default value."))
                      (do
                        (reset! store (cljs.reader/read-string store-str))
                        (conj-err "Read from localstorage.")))))))))

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
  (cljs.reader/read-string (pr-str (pop (conj #queue [1,2,3] [1])))))

; ---Connect to browser REPL
(comment
  (js/alert "foo")
  ;; browser-REPL
  :cljs/quit
  (do
    (clojure.core/require 'shadow.cljs.devtools.api)
    (shadow.cljs.devtools.api/repl :app))
  
  ,)
