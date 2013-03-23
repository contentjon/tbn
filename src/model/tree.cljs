(ns model.tree
  (:require [graphite.db :as db]))

(defprotocol Tree
  (children [_]))

(defn add-transform [db key transform callback]
  (db/update db key
             (fn [x]
               (update-in transform [:inputs]
                          (fn [in]
                            (vec (concat [x] in)))))
             callback))
