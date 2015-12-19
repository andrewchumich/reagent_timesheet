(ns tsheets.components.timesheet
  (:require [tsheets.components.notes :refer [notes-component]]
            [tsheets.components.clock-in :refer [clock-in-component]]))

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

(defn is-selected? [{:keys [parent child list]}]
  (do
    (if (nil? child)
      false
      (if (= (:id child) (:id parent))
        true
        (if (nil? (:parent-id child)) 
          false
          (is-selected? {:parent parent
                         :child ((:parent-id child) list)}))))))

(defn managed-list-component [{:keys [list current-id on-select on-select-parent is-visible?]}]
  [:div
   [:button {:type "button"
             :disabled false
             :on-click #(on-select-parent :0)} "Back"]
   [:button {:type "button"
             :disabled (nil? current-id)
             :on-click #(on-select nil)
             } "X"]
   (for [list-item (seq (into (sorted-map) list))]
     (if (is-visible? (val list-item))
       [:div {:key (key list-item)}
        [:input {:type "button"
                 :class (str "jobcode-list  " (if (is-selected? {:parent (val list-item)
                                                                 :child (if (nil? current-id)
                                                                          nil
                                                                          (current-id list))
                                                                 :list list}) "selected"))
                 :id (key list-item)
                 :value (:name (val list-item))
                 :on-click #(if (true? (:has-children (val list-item)))
                              (on-select-parent (key list-item))
                              (on-select (key list-item)))}]])
     )
   ])

(defn custom-field-items-component [{:keys [custom-field-items current-id on-select on-select-parent custom-field-id]}]
  [:div {:class "container"}
   [managed-list-component {:list custom-field-items
                            :on-select on-select
                            :on-select-parent on-select-parent
                            :current-id current-id
                            :is-visible? #(= (:custom-field-id %) custom-field-id)}]
   ])

(defn custom-fields-component [{:keys [custom-fields custom-field-items current-custom-fields on-select]}]
  [:div {:class "container"};
   (for [custom-field (seq (into (sorted-map) custom-fields))]
     [:div {:key (key custom-field)} 
      [:p {:class "title"} (:name (val custom-field))]
      (case (:type (val custom-field))
        "managed-list" (custom-field-items-component {:custom-field-items custom-field-items
                                                      :custom-field-id (key custom-field)
                                                      :current-id ((key custom-field) current-custom-fields)
                                                      :on-select #(on-select {:custom-field-id (key custom-field)
                                                                              :custom-field-item-id %}) 
                                                      })
        nil)])])

(defn jobcode-component [{:keys [jobcodes current-id on-select on-select-parent parent-id]}]
  [:div {:class "container"}
   [:p "Jobcodes"]
   [managed-list-component {:list jobcodes
                            :on-select on-select
                            :on-select-parent on-select-parent
                            :is-visible? #(= (:parent-id %) parent-id)
                            :current-id current-id}]
   ]
  )

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
