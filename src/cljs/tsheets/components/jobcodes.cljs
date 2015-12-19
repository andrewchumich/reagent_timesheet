(ns tsheets.components.jobcodes
  (:require [tsheets.components.managed-list :refer [managed-list-component]]))

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
