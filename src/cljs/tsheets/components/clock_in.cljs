(ns tsheets.components.notes
  (:require [reagent.core :as reagent :refer [atom]]))

(defn clock-in-component [{:keys [clocked-in on-clock-out on-clock-in]}]
  (if (true? clocked-in)
    [:div
     [:input {:type "button"
              :value "Clock Out"
              :class "clocked-in"
              :on-click #(on-clock-out)}]]
    [:div
     [:input {:type "button"
              :class "clocked-out"
              :value "Clock In"
              :on-click #(on-clock-in)}]]))
