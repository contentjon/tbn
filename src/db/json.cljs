(ns db.json
  (:require [db.core :as db]))

(defn log [& msg]
  (->> msg
       (apply println-str)
       (.log js/console)))

(defn logger [msg]
  (fn [x]
    (log msg x)
    x))


(defn not-found? [x]
  (= (str x) (str db/not-found)))

(defrecord JsonWrappedDB [db]
  db/AsyncDB
  (-read
   [_ key callback]
   (db/-read db key (fn [x]
                      (callback
                       (if (not-found? x)
                         db/not-found
                         (-> x JSON/parse js->clj))))))
  (-write
   [_ key value callback]
   (let [val (-> value clj->js JSON/stringify)]
     (db/-write db key val callback)))
  (-delete
   [_ key callback]
   (db/-delete db key callback)))

(def wrap ->JsonWrappedDB)
