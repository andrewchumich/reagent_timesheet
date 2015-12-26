(ns tsheets.cards
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [tsheets.components.datetime-picker :refer [datetime-picker-component picker-component]]
            [cljs-time.core :as time])
  (:require-macros
   [devcards.core
    :as dc
    :refer [defcard defcard-doc defcard-rg deftest]]))

(defcard-rg picker-card
  "This card displays data and calls an 'up' and 'down' function" 
  [picker-component {:data "hello" 
                     :on-up #(println "UP")
                     :on-down #(println "DOWN")}])

(defcard-rg datetime-picker-card
  "#Timepicker Component"
  (fn [some-time _] (datetime-picker-component {:current-time @some-time
                                     :on-change #(reset! some-time %)}))
  (atom (time/now)))

(reagent/render [:div] (.getElementById js/document "app"))

;; remember to run 'lein figwheel devcards' and then browse to
;; http://localhost:3449/cards
