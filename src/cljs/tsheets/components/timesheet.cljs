(ns tsheets.components.timesheet
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :as async
             :refer [<! >! chan put! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(defn set-notes [ts notes]
  (swap! ts assoc-in [:notes] notes))

(enable-console-print!)
(defn select-jobcode [ts jobcode]
  (swap! ts assoc-in [:jobcode] jobcode))

(defn reset-jobcode-state [jobcode-state]
  (reset! jobcode-state {:parent-id :0}))

(defn valid-clock-in? [ts]
  (if (keyword? (:jobcode @ts))
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

(defn get-jobcode [{:keys [jobcode-id jobcodes]}]
  true)

(defn is-selected? [{:keys [parent child jobcodes]}]
  (if (nil? child)
    false
    (if (= (:id child) (:id parent))
      true
      (is-selected? {:parent parent
                     :child ((:parent-id child) jobcodes)}))))

(defn notes-component [{:keys [notes on-save on-stop on-change]}]
  [:div {:class "container"}
   [:input {:type "text"
            :value notes
            :on-change #(on-change (-> % .-target .-value))
            :on-key-down #(case (.-which %)
                            27 (on-change "")
                            nil)
            }]
   ])

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

(defn jobcode-component [{:keys [jobcodes timesheet on-select-child on-select-parent jobcode-state]}]
  (let []
    [:div {:class "container"}
     [:p "Jobcodes"]
     [:button {:type "button"
               :disabled (= :0 (:parent-id jobcode-state))
               :on-click #(on-select-parent :0)} "Back"]
     (for [jobcode (seq (into (sorted-map) jobcodes))]
       (if (true? (= (:parent-id (val jobcode)) (:parent-id jobcode-state)))
         [:div {:key (key jobcode)}
          [:input {:type "button"
                   :class (str "jobcode-list  " (if (is-selected? {:parent (val jobcode)
                                                                   :child (let [child-id (:jobcode timesheet)]
                                                                            (if (nil? child-id)
                                                                              nil
                                                                              (child-id jobcodes)))
                                                                   :jobcodes jobcodes}) "selected"))
                   :id (key jobcode)
                   :value (:name (val jobcode))
                   :on-click #(if (true? (:has-children (val jobcode)))
                                (on-select-parent (key jobcode))
                                (on-select-child (key jobcode)))}]])
       )])
  )

(defn timesheet-component [{:keys [timesheet jobcodes jobcode-state on-clock-out]}]
  (println @timesheet)
  (let []
    [:div
     [jobcode-component {:jobcodes @jobcodes
                         :timesheet @timesheet
                         :on-select-child #(do
                                             (reset-jobcode-state jobcode-state)
                                             (select-jobcode timesheet %))
                         :on-select-parent #(swap! jobcode-state assoc-in [:parent-id] %)
                         :jobcode-state @jobcode-state}]
     [notes-component {:notes (:notes @timesheet)
                       :on-change #(set-notes timesheet %)
                       :on-save #(set-notes timesheet %)}]
     [clock-in-component {:timesheet timesheet
                          :clocked-in (is-clocked-in? timesheet)
                          :on-clock-in #(clock-in timesheet)
                          :on-clock-out #(do ((clock-out timesheet)
                                              (on-clock-out)))}]]))
