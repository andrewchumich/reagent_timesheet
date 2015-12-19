(ns tsheets.components.custom-fields
  (:require [tsheets.components.managed-list :refer [managed-list-component]]))

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

