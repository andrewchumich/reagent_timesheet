(ns tsheets.core 
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [cljs.core.async :as async
             :refer [<! >! chan put! timeout]]
            [tsheets.components.timecard :refer [timecard-component]]
            [tsheets.components.timesheet :refer [timesheet-component]]
            [tsheets.utils.timesheet-validation :refer [valid-clock-out? valid-clock-in? valid-submit?]]
            [tsheets.utils.timesheet :refer [reset-timesheet
                                             update-timesheet!
                                             set-notes!
                                             set-jobcode!
                                             set-custom-field!
                                             clocked-in?
                                             clock-in!
                                             clock-out!
                                             set-start!
                                             set-end!]]
            [cljs-uuid-utils.core :as uuid]
            [cljs-time.core :as time])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)
(defn make-uuid-keyword []
  (keyword (uuid/uuid-string (uuid/make-random-uuid))))

(defn reset-jobcode-state
  ([] (do
        {:parent-ids {:add-timesheet :0
                      :edit-timesheet :0
                      :timecard :0}
         :jobcodes {:000 {:name "Flipping Burgers"
                          :id :000
                          :has-children false
                          :parent-id :0}
                    :001 {:name "Being Awesome"
                          :id :001
                          :has-children false
                          :parent-id :0}
                    :002 {:name "Exercising"
                          :id :002
                          :has-children true
                          :parent-id :0}
                    :003 {:name "Running"
                          :id :003
                          :has-children false
                          :parent-id :002}
                    :004 {:name "Lifting Weights"
                          :id :004
                          :has-children false
                          :parent-id :002}}}
        ))
  ([jobcode-state]
   (reset! jobcode-state (reset-jobcode-state))))

(defn reset-custom-field-state
  ([] (do
        {:custom-fields {:000 {:name "Not Req 1"
                               :id :000
                               :required false
                               :type "managed-list"}
                         :001 {:name "Male Models"
                               :id :001
                               :required true
                               :type "managed-list"}}
         :custom-field-items {:000 {:name "Hello"
                                    :id :000
                                    :custom-field-id :000}
                              :001 {:name "World"
                                    :id :001
                                    :custom-field-id :000}
                              :002 {:name "Derek Zoolander"
                                    :id :002
                                    :custom-field-id :001}
                              :003 {:name "Hansel"
                                    :id :003
                                    :custom-field-id :001}}}
        ))
  ([custom-field-state]
   (reset! custom-field-state (reset-custom-field-state))))

(defonce timesheet-atom (atom (reset-timesheet)))
(defonce timecard-atom (atom (reset-timesheet)))
(defonce timesheet-list-atom (atom {}))

(defonce jobcode-state-atom (atom (reset-jobcode-state)))
(defonce custom-field-state-atom (atom (reset-custom-field-state)))

(def save-buffer (chan))
(go (while true
      (let [timesheet (<! save-buffer)
            local-id (make-uuid-keyword)] 
        (println (str "COMPLETE TIMESHEET " timesheet))
        (swap! timesheet-list-atom assoc-in [local-id] (assoc-in timesheet [:local-id] local-id))))
    )
(defn save-timesheet [ts]
  (go (>! save-buffer ts)))

;; function to manipulate app state


(defn get-jobcode [{:keys [jobcode-id jobcodes]}]
  true)

(defn set-jobcode-parent! [{:keys [jobcode-state parent-id type]}]
  (swap! jobcode-state assoc-in [:parent-ids type] parent-id))
;; -------------------------
;; Views

(defn timecard-page []
  [:div
   [timecard-component {:timesheet @timecard-atom
                        :jobcodes (:jobcodes @jobcode-state-atom)
                        :jobcode-parent-id (:timecard (:parent-ids @jobcode-state-atom))
                        :on-select-jobcode #(do 
                                              (set-jobcode-parent! {:jobcode-state jobcode-state-atom
                                                                    :parent-id :0
                                                                    :type :timecard})
                                              (set-jobcode! timecard-atom %))
                        :on-select-jobcode-parent #(set-jobcode-parent! {:jobcode-state jobcode-state-atom
                                                                         :parent-id %
                                                                         :type :timecard})
                        :on-set-notes #(set-notes! timecard-atom %)
                        :custom-field-state @custom-field-state-atom
                        :on-select-custom-field #(set-custom-field! timecard-atom %)
                        :on-clock-in #(if (valid-clock-in? @timecard-atom) 
                                        (clock-in! timecard-atom))
                        :on-clock-out #(if (valid-clock-out? {:timesheet @timecard-atom
                                                              :jobcodes (:jobcodes @jobcode-state-atom)
                                                              :custom-fields (:custom-fields @custom-field-state-atom)}) 
                                         (do 
                                           (save-timesheet @timecard-atom)
                                           (set-jobcode-parent! {:jobcode-state jobcode-state-atom
                                                                 :parent-id :0
                                                                 :type :timecard})
                                           (reset-timesheet timecard-atom)))
                        :clocked-in? (clocked-in? @timecard-atom)}]
   [:div [:a {:href "/about"} "go to about page"]]
   [:div [:a {:href "/add"} "add timesheet"]]])

(defn add-timesheet-page []
  [:div
   [timesheet-component {:timesheet @timesheet-atom
                         :jobcodes (:jobcodes @jobcode-state-atom)
                         :jobcode-parent-id (:add-timesheet (:parent-ids @jobcode-state-atom))
                         :on-select-jobcode #(do
                                               (set-jobcode-parent! {:jobcode-state jobcode-state-atom
                                                                     :parent-id :0
                                                                     :type :add-timesheet})
                                               (set-jobcode! timesheet-atom %))
                         :on-select-jobcode-parent #(set-jobcode-parent! {:jobcode-state jobcode-state-atom
                                                                          :parent-id %
                                                                          :type :add-timesheet})
                         :on-set-notes #(set-notes! timesheet-atom %)
                         :custom-field-state @custom-field-state-atom
                         :on-select-custom-field #(set-custom-field! timesheet-atom %)
                         :on-set-start #(set-start! timesheet-atom %)
                         :on-set-end #(set-end! timesheet-atom %)
                         :on-submit #(if (valid-submit? {:timesheet @timesheet-atom
                                                         :jobcodes (:jobcodes @jobcode-state-atom)
                                                         :custom-fields (:custom-fields @custom-field-state-atom)}) 
                                       (do 
                                         (save-timesheet @timesheet-atom)
                                         (set-jobcode-parent! {:jobcode-state jobcode-state-atom
                                                               :parent-id :0
                                                               :type :add-timesheet})
                                          (reset-timesheet timesheet-atom)))}]
   [:div [:a {:href "/about"} "go to about page"]]
   [:div [:a {:href "/"} "see timecard"]]])

(defn edit-timesheet-page [id]
  (let [timesheet (atom ((keyword id) @timesheet-list-atom))
        timesheet-id (keyword id)] 
    (fn [] 
      [:div
       [timesheet-component {:timesheet @timesheet
                             :jobcodes (:jobcodes @jobcode-state-atom)
                             :jobcode-parent-id (:edit-timesheet (:parent-ids @jobcode-state-atom))
                             :on-select-jobcode #(do
                                                   (set-jobcode-parent! {:jobcode-state jobcode-state-atom
                                                                         :parent-id :0
                                                                         :type :edit-timesheet})
                                                   (set-jobcode! timesheet %))
                             :on-select-jobcode-parent #(set-jobcode-parent! {:jobcode-state jobcode-state-atom
                                                                              :parent-id %
                                                                              :type :edit-timesheet})
                             :on-set-notes #(set-notes! timesheet %)
                             :custom-field-state @custom-field-state-atom
                             :on-select-custom-field #(set-custom-field! timesheet %)
                             :on-set-start #(set-start! timesheet %)
                             :on-set-end #(set-end! timesheet %)
                             :on-submit #(update-timesheet! {:list timesheet-list-atom
                                                            :id timesheet-id
                                                            :timesheet @timesheet})}]
       [:div [:a {:href "/about"} "go to about page"]]
       [:div [:a {:href "/"} "see timecard"]]]))
  )

(defn about-page []
  [:div
   [:h2 "About tsheets"]
   [:div {:class "timesheet-list"}
    (for [timesheet (seq (into (sorted-map) @timesheet-list-atom))]
     [:div {:key (key timesheet)
            :class "timesheet-row"}
      [:p (:notes (val timesheet))]
      [:a {:href (str "/edit/" (name (key timesheet)))} "edit"]])]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #(timecard-page)))

(secretary/defroute "/add" []
  (session/put! :current-page #(add-timesheet-page)))

(secretary/defroute "/edit/:id" [id]
  (session/put! :current-page #(edit-timesheet-page id)))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
