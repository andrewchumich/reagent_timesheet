(ns tsheets.components.timesheet
  (:require [tsheets.components.notes :refer [notes-component]]
            [tsheets.components.clock-in :refer [clock-in-component]]
            [tsheets.components.managed-list :refer [managed-list-component]]
            [tsheets.components.jobcodes :refer [jobcode-component]]
            [tsheets.components.custom-fields :refer [custom-fields-component]]
            [tsheets.components.datetime-picker :refer [datetime-picker-component]]))
(enable-console-print!)

(defn timesheet-component [{:keys [timesheet
                                   jobcodes
                                   jobcode-parent-id
                                   on-select-jobcode
                                   on-select-jobcode-parent
                                   custom-field-state 
                                   on-select-custom-field
                                   on-set-notes
                                   on-set-start
                                   on-set-end
                                   on-submit]}]
  (println timesheet)
  [:div
   [jobcode-component {:jobcodes jobcodes
                       :current-id (:jobcode timesheet)
                       :on-select on-select-jobcode
                       :on-select-parent on-select-jobcode-parent
                       :parent-id jobcode-parent-id}]
   [notes-component {:notes (:notes timesheet)
                     :on-change on-set-notes
                     :on-save on-set-notes}]
   [custom-fields-component {:custom-fields (:custom-fields custom-field-state)
                             :custom-field-items (:custom-field-items custom-field-state)
                             :current-custom-fields (:custom-fields timesheet)
                             :on-select on-select-custom-field}]
   [:div
    [:p "Start"]
    [datetime-picker-component {:current-time (:start timesheet)
                                :on-change #(on-set-start %)}]]
   [:div
    [:p "End"]
    [datetime-picker-component {:current-time (:end timesheet)
                                :on-change #(on-set-end %)}]]
   [:input {:type "button"
            :on-click on-submit
            :value "Submit"}]])
