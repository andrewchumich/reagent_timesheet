(ns tsheets.core 
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [cljs.core.async :as async
             :refer [<! >! chan put! timeout]]
            [tsheets.components.timesheet :as timesheets-components
             :refer [timesheet-component]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

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

(defn reset-jobcode-state
  ([] (do
        {:parent-id :0
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
        {:parent-ids {:000 :0
                      :001 :0}
         :custom-fields {:000 {:name "Not Req 1"
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

(defonce jobcode-state-atom (atom (reset-jobcode-state)))
(defonce custom-field-state (atom (reset-custom-field-state)))

(def save-buffer (chan))
(go (while true
      (println (str "COMPLETE TIMESHEET " (<! save-buffer))))
    )
(defn save-timesheet [ts-ref]
  (go (>! save-buffer ts-ref)))

;; -------------------------
;; Views

(defn home-page []
  [:div
   [timesheet-component {:timesheet timesheet-atom
                         :jobcode-state jobcode-state-atom
                         :custom-field-state custom-field-state
                         :on-clock-out #(do (save-timesheet @timesheet-atom)
                                            (reset-jobcode-state jobcode-state-atom)
                                            (reset-timesheet timesheet-atom))}]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div
   [:h2 "About tsheets"]
   
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

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
