(ns rifuta
  (:require [replicant.dom :as r]))

(defn render-logset-form [& args] ;should return the HTML only.
  [:div#all
   [:div#exercise "Exercise Name: " [:input {:type "text"}]]
   [:div#weight "Weight: " [:input {:type "number"}]]
   [:div#note "Note: " [:input {:type "text"}]]
   [:div#submit [:button {:on {:click
                               (fn [e] ;--submits to the atom, call button logic here.
                                 )}}
                 "Submit"]]])

;Show the previous set. Can't render with the same function, so look into if it can be done with mulitple render functions or need to tweak the index.html to include more divs.
(defn render-prevset-form [& args])

(defn button-logic [& args]
  ;something related to button goes here.
  )

(defn main []
  (let [el (js/document.getElementById "app")]
    (r/render el (render-logset-form "lmao")))) ;always 'CALL' the render function.

; --- Testing code from here
(comment
  (defn render-testbox [data] ;Test render function
    [:div#div0
     [:div#div1 [:input {:type "text"}]]
     [:div#div2 [:p#p1 data]]
     [:div#div3 [:button {:on {:click
                               (fn [e]
                                 (let [el2 (js/document.getElementById "p1")]
                                   (set! (.-innerText el2) "lol")))}}
                 "lule"]]]))
