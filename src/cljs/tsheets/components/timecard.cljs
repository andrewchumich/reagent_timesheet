(ns tsheets.components.timecard
  (:require [tsheets.components.notes :refer [notes-component]]
            [tsheets.components.clock-in :refer [clock-in-component]]
            [tsheets.components.managed-list :refer [managed-list-component]]
            [tsheets.components.jobcodes :refer [jobcode-component]]
            [tsheets.components.custom-fields :refer [custom-fields-component]]))
(enable-console-print!)

(defn timecard-component [{:keys [timesheet
                                   jobcodes
                                   jobcode-parent-id
                                   on-select-jobcode
                                   on-select-jobcode-parent
                                   custom-field-state 
                                   on-select-custom-field
                                   on-set-notes
                                   on-clock-out
                                   on-clock-in
                                   clocked-in?]}]
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
   [clock-in-component {:clocked-in clocked-in?
                        :on-clock-in on-clock-in
                        :on-clock-out on-clock-out}]])
