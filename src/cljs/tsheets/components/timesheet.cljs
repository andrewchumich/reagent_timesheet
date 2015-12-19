(ns tsheets.components.timesheet
  (:require [tsheets.components.notes :refer [notes-component]]
            [tsheets.components.clock-in :refer [clock-in-component]]
            [tsheets.components.managed-list :refer [managed-list-component]]
            [tsheets.components.jobcodes :refer [jobcode-component]]
            [tsheets.components.custom-fields :refer [custom-fields-component]]))

(defn set-notes [ts notes]
  (swap! ts assoc-in [:notes] notes))

(enable-console-print!)

(defn select-jobcode [ts jobcode]
  (swap! ts assoc-in [:jobcode] jobcode))

(defn select-custom-field [ts custom-field]
  (println custom-field)
  (let [custom-field-id (:custom-field-id custom-field)
        custom-field-item-id (:custom-field-item-id custom-field)] 
    (swap! ts assoc-in [:custom-fields custom-field-id] custom-field-item-id)))

(defn reset-jobcode-state [jobcode-state]
  (swap! jobcode-state assoc-in [:parent-id] :0))

(defn valid-clock-in? [ts]
  (if (keyword? (:jobcode ts))
    true
    false))

(defn is-clocked-in? [ts]
  (and (nil? (:end ts)) (= (type (:start ts)) js/Date)))

(defn valid-clock-out? [ts]
  (if (and (valid-clock-in? ts) (is-clocked-in? ts))
    true
    false
    ))

(defn clock-in [ts]
  (swap! ts assoc-in [:start] (new js/Date)))

(defn clock-out [ts]
  (swap! ts assoc-in [:end] (new js/Date)))

(defn get-jobcode [{:keys [jobcode-id jobcodes]}]
  true)

(defn timesheet-component [{:keys [timesheet jobcode-state custom-field-state on-clock-out]}]
  (println @timesheet)
  [:div
   [jobcode-component {:jobcodes (:jobcodes @jobcode-state)
                       :current-id (:jobcode @timesheet)
                       :on-select #(do
                                     (reset-jobcode-state jobcode-state)
                                     (select-jobcode timesheet %))
                       :on-select-parent #(swap! jobcode-state assoc-in [:parent-id] %)
                       :parent-id (:parent-id @jobcode-state)}]
   [notes-component {:notes (:notes @timesheet)
                     :on-change #(set-notes timesheet %)
                     :on-save #(set-notes timesheet %)}]
   [custom-fields-component {:custom-fields (:custom-fields @custom-field-state)
                             :custom-field-items (:custom-field-items @custom-field-state)
                             :current-custom-fields (:custom-fields @timesheet)
                             :parent-ids (:parent-ids @custom-field-state)
                             :on-select #(do 
                                           (select-custom-field timesheet %))}]
   [clock-in-component {:clocked-in (is-clocked-in? @timesheet)
                        :on-clock-in #(if (valid-clock-in? @timesheet) 
                                        (clock-in timesheet))
                        :on-clock-out #(do (if (valid-clock-out? @timesheet)
                                             ((clock-out timesheet)
                                              (on-clock-out))
                                             ))}]])
