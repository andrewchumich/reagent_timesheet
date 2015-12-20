(ns tsheets.utils.timesheet-validation)

(defn valid-date? [date] (= (type date) js/Date))

(defn valid-jobcode? [{:keys [jobcode-id jobcodes]}]
  "The jobcode id needs to be in the map of valid jobcodes"
  (if (nil? jobcode-id)
    false
    (not (nil? (jobcode-id jobcodes)))))

(defn valid-custom-fields? [{:keys [custom-field-ids custom-fields custom-field-items]}]
  (println custom-field-ids)
  true)

(defn valid-clock-out? [{:keys [timesheet jobcodes custom-fields custom-field-items]}]
  (and (valid-date? (:start timesheet)) 
       (valid-jobcode? {:jobcode-id (:jobcode timesheet)
                        :jobcodes jobcodes})
       (valid-custom-fields? {:custom-field-ids (:custom-fields timesheet)
                              :custom-fields custom-fields
                              :custom-field-items custom-field-items})))