(ns tsheets.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [cljs.core.async :as async
               :refer [<! >! chan put! timeout]])
    (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(defn reset-timesheet
  ([] (do
        (println "RESET W/O ARGS")
        {:start nil
         :end nil
         :jobcode nil
         :notes ""}))
  ([ts] (do
          (println "RESET W ARGS")
          (reset! ts (reset-timesheet)))))

(defonce timesheet (atom (reset-timesheet)))

(defonce jobcodes (atom ["Flipping Burgers"
                         "Sweeping Floors"]))

(defn is-clocked-in? [ts]
  (= (type (:start @ts)) js/Date))

(defn valid-clock-in? [ts]
  (if (string? (:jobcode @timesheet))
    true
    false))

(defn valid-clock-out? [ts]
  (if (and (valid-clock-in? ts) (is-clocked-in? ts))
    true
    false
    ))

(def save-buffer (chan))
(go (while true
      (println (str "COMPLETE TIMESHEET" (<! save-buffer))))
    )
(defn save-timesheet [ts]
  (go (>! save-buffer ts)))

(defn clock-in [ts]
  (if (valid-clock-in? ts) 
    (swap! ts assoc-in [:start] (new js/Date))
    ))

(defn clock-out [ts]
  (if (valid-clock-out? ts)
    (do
      (swap! ts assoc-in [:end] (new js/Date))
      (save-timesheet @ts)
      (reset-timesheet ts)
      )
    )
  )

(defn notes-component [{:keys [notes on-save on-stop]}]
  (let [val (atom notes)
        stop #(do (reset! val "")
                  (if on-stop (on-stop)))
        save #(let [v (-> @val str clojure.string/trim)]
                (if-not (empty? v) (on-save v)))]
    (fn [] [:div {:class "container"}
            [:input {:type "text"
                     :value @val
                     :on-blur save
                     :on-change #(do
                                   (reset! val (-> % .-target .-value)))
                     :on-key-down #(case (.-which %)
                                     13 (save)
                                     27 (stop)
                                     nil)}]
            ])))

(defn select-jobcode [ts jobcode]
  (if (string? jobcode)
    (swap! ts assoc-in [:jobcode] jobcode)))

(defn set-notes [ts notes]
  (swap! ts assoc-in [:notes] notes))
;; -------------------------
;; Views

(defn clock-in-component [ts]
  (if (is-clocked-in? ts)
    [:div
     [:input {:type "button"
              :value "Clock Out"
              :class "clocked-in"
              :on-click #(clock-out ts)}]]
    [:div
     [:input {:type "button"
              :class "clocked-out"
              :value "Clock In"
              :on-click #(clock-in ts)}]]))

(defn jobcode-component [ts jb]
  (let [t @ts]
    [:div {:class "container"}
     [:p "Jobcodes"]
     (for [jobcode @jb]
       [:div {:key jobcode}
        [:input {:type "button"
                 :class (str "jobcode-list  " (if (= jobcode (:jobcode t)) "selected"))
                 :value jobcode
                 :on-click #(select-jobcode ts (-> % .-target .-value))}]]
       )]
    ))


(defn timecard [ts jb]
  (do
    (println @ts)
    [:div {:class "container"}
     [jobcode-component ts jb]
     [notes-component {:on-save #(set-notes ts %)
                       :notes (:notes @ts)}]
     [clock-in-component ts]]
    ))

(defn home-page []
  [:div [:h2 "Welcome to tsheets"]
   [timecard timesheet jobcodes]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div
   [notes-component]
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
