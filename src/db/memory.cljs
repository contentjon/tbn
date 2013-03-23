(ns db.memory
  (:require [db.core :as db]))

(extend-protocol db/AsyncDB
  cljs.core.Atom
  (-read
   [a key callback]
   (callback
    (if (contains? @a key)
      (get @a key)
      db/not-found)))
  (-write
   [a key value callback]
   (swap! a assoc key value)
   (callback value))
  (-delete
   [a key callback]
   (swap! a dissoc key)
   (callback)))

(defn make []
  (atom {}))
