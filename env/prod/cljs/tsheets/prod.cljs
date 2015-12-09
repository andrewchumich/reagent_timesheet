(ns tsheets.prod
  (:require [tsheets.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
