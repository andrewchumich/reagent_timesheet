(ns tsheets.components.managed-list
  (:require [reagent.core :as reagent :refer [atom]]))

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

(defn managed-list-component []
  (let [expanded (atom false)
        expand #(do 
                  (reset! expanded true)
                  nil)
        collapse #(do
                    (reset! expanded false)
                    nil)] 
    (fn [{:keys [list current-id on-select on-select-parent is-visible?]}] 
      [:div
       (if (true? @expanded) 
         [:input {:type "button"
                  :on-click (if (nil? on-select-parent) 
                              collapse)
                  :value "Back"}]
         [:input {:type "button"
                  :on-click expand
                  :class (if (not (nil? current-id)) "selected")
                  :value (if (nil? current-id)
                           "Select"
                           (:name (current-id list)))}])
       [:input {:type "button"
                 :disabled (nil? current-id)
                 :on-click #(do
                              (collapse)
                              (on-select nil))
                 :value "X"}]
       (if (true? @expanded) 
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
                                         (do 
                                           (collapse)
                                           (on-select (key list-item))))}]])
                ))
       ])))
