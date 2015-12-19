(ns tsheets.components.notes)

(defn thing [] "THING")

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
