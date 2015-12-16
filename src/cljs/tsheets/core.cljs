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
         :notes ""}))
  ([timesheet] (do
          (reset! timesheet (reset-timesheet)))))

(defn reset-jobcode-state
  ([] (do
        {:parent-id :0}
        ))
  ([jobcode-state]
   (reset! jobcode-state (reset-jobcode-state))))

(defonce timesheet-atom (atom (reset-timesheet)))

(defonce jobcodes-atom (atom {:000 {:name "Flipping Burgers"
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
                                    :parent-id :002}}))

(defonce jobcode-state-atom (atom (reset-jobcode-state)))

(def save-buffer (chan))
(go (while true
      (println (str "COMPLETE TIMESHEET " (<! save-buffer))))
    )
(defn save-timesheet [ts-ref]
  (go (>! save-buffer ts-ref)))

;; -------------------------
;; Views

(defn timecard [{:keys [timesheet jobcodes]}]
  (do
    (println @timesheet)
    [:div {:class "container"}
     [timesheet-component {:timesheet timesheet
                           :jobcodes jobcodes}]]
    ))

(defn home-page []
  [:div
   [timesheet-component {:timesheet timesheet-atom
                         :jobcodes jobcodes-atom
                         :jobcode-state jobcode-state-atom
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
