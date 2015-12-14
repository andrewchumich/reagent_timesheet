(ns tsheets.components.timesheet
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :as async
             :refer [<! >! chan put! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn set-notes [ts notes]
  (swap! ts assoc-in [:notes] notes))

(enable-console-print!)
(defn select-jobcode [ts jobcode]
  (if (string? jobcode)
    (swap! ts assoc-in [:jobcode] jobcode)))


(defn valid-clock-in? [ts]
  (if (string? (:jobcode @ts))
    true
    false))


(defn is-clocked-in? [ts]
  (= (type (:start @ts)) js/Date))

(defn valid-clock-out? [ts]
  (if (and (valid-clock-in? ts) (is-clocked-in? ts))
    true
    false
    ))

(defn clock-in [ts]
  (if (valid-clock-in? ts) 
    (swap! ts assoc-in [:start] (new js/Date))
    ))

(defn clock-out [ts]
  (if (valid-clock-out? ts)
    (do
      (swap! ts assoc-in [:end] (new js/Date))
      )
    )
  )

(defn notes-component [{:keys [notes on-save on-stop on-change]}]
  (println notes)
  [:div {:class "container"}
   [:input {:type "text"
            :value notes
            
            :on-change #(on-change (-> % .-target .-value))
            }]
   ])

(defn clock-in-component [{:keys [timesheet clocked-in on-clock-out on-clock-in]}]
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

(defn jobcode-component [{:keys [jobcodes timesheet on-select]}]
  (let [ts @timesheet]
    [:div {:class "container"}
     [:p "Jobcodes"]
     (for [jobcode @jobcodes]
       [:div {:key jobcode}
        [:input {:type "button"
                 :class (str "jobcode-list  " (if (= jobcode (:jobcode ts)) "selected"))
                 :value jobcode
                 :on-click #(on-select (-> % .-target .-value))}]]
       )]
    ))

(defn timesheet-component [{:keys [timesheet jobcodes on-clock-out]}]
  (println @timesheet)
  (let []
    [:div
     [jobcode-component {:jobcodes jobcodes
                         :timesheet timesheet
                         :on-select #(select-jobcode timesheet %)}]
     [notes-component {:notes (:notes @timesheet)
                       :on-change #(set-notes timesheet %)
                       :on-save #(set-notes timesheet %)}]
     [clock-in-component {:timesheet timesheet
                          :clocked-in (is-clocked-in? timesheet)
                          :on-clock-in #(clock-in timesheet)
                          :on-clock-out #(do ((clock-out timesheet)
                                              (on-clock-out)))}]]))
