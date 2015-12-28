(ns tsheets.components.datetime-picker
  (:require             
   [cljs-time.core :as time]))

(defn picker-component [{:keys [data on-up on-down]}]
  [:div {:class "picker"}
   [:input {:type "button"
            :on-click on-up
            :value "Up"
            :class "picker-up"}]
   [:div data]
   [:input {:type "button"
            :on-click on-down
            :value "Down"
            :class "picker-down"}]])

(defn time-picker-component [{:keys [current-time on-change]}]
  [:div {:class "datetime-picker-time"}
   [:p "Time"]
   [picker-component {:data (time/hour current-time)
                      :on-up #(on-change (time/plus current-time (time/hours 1)))
                      :on-down #(on-change (time/minus current-time (time/hours 1)))}]
   [picker-component {:data (time/minute current-time)
                      :on-up #(on-change (time/plus current-time (time/minutes 1)))
                      :on-down #(on-change (time/minus current-time (time/minutes 1)))}] 
   ])

(defn datetime-picker-component [{:keys [current-time on-change]}] 
  (if (nil? current-time) 
    (do 
      (on-change (time/now))
      nil)
    [:div {:class "datetime-picker"}
          [:div {:class "datetime-picker-date"}
           [:p "Date"]
           [picker-component {:data (time/year current-time)
                              :on-up #(on-change (time/plus current-time (time/years 1)))
                              :on-down #(on-change (time/minus current-time (time/years 1)))}]
           [picker-component {:data (time/month current-time)
                              :on-up #(on-change (time/plus current-time (time/months 1)))
                              :on-down #(on-change (time/minus current-time (time/months 1)))}]
           [picker-component {:data (time/day current-time)
                              :on-up #(on-change (time/plus current-time (time/days 1)))
                              :on-down #(on-change (time/minus current-time (time/days 1)))}]]
          [time-picker-component {:current-time current-time
                                  :on-change on-change}]
          ]))
