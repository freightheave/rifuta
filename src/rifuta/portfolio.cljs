(ns rifuta.portfolio
  (:require [portfolio.ui :as portfolio]
            [portfolio.replicant :refer-macros [defscene]]
            [rifuta.ui :as ui]))

(defscene empty-cell
  (ui/render-log-set {}))

(defn main []
  (portfolio/start!
   {:config
    {:css-paths ["/styles.css"]
     :viewport/defaults
     {:background/background-color "#fdeddd"}}}))
