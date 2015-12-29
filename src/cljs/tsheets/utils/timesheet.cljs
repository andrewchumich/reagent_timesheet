(ns tsheets.utils.timesheet
  (:require             
   [cljs-time.core :as time]))

(defn reset-timesheet
  ([] (do
        {:start nil
         :end nil
         :jobcode nil
         :custom-fields {:000 nil
                         :001 nil}
         :notes ""}))
  ([timesheet] (do
                 (reset! timesheet (reset-timesheet)))))


(defn update-timesheet! [{:keys [list id timesheet]}]
  (swap! list assoc-in [id] timesheet))


(defn set-notes! [ts notes]
  (swap! ts assoc-in [:notes] notes))

(defn set-jobcode! [ts jobcode]
  (swap! ts assoc-in [:jobcode] jobcode))

(defn set-custom-field! [ts custom-field]
  (let [custom-field-id (:custom-field-id custom-field)
        custom-field-item-id (:custom-field-item-id custom-field)] 
    (swap! ts assoc-in [:custom-fields custom-field-id] custom-field-item-id)))


(defn clock-in! [ts]
  (swap! ts assoc-in [:start] (time/now)))

(defn clock-out! [ts]
  (swap! ts assoc-in [:end] (time/now)))

(defn clocked-in? [ts] 
  (and
   (not (nil? (:start ts)))
   (nil? (:end ts))))

(defn set-start! [ts start]
  (swap! ts assoc-in [:start] start))

(defn set-end! [ts end]
  (swap! ts assoc-in [:end] end))
