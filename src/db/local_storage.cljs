(ns db.local-storage
  (:require [db.core :as db]
            [model.command :as cmd]))

(def local-storage (js/localStorage))

(defn next-store [parent key & [init]]
  (let [prop (name key)
        init (or init (js-obj))]
    (or (aget parent prop)
        (let [obj init]
          (aset parent prop init)
          init))))

(defn store-in [path]
  (reduce next-store local-storage path))

(defn inc-count [coll]
  (let [old (or (aget coll "count")
                0)]
    (aset coll "count" (inc old))
    old))

(defn add-to-coll [coll model-data]
  (let [id  (inc-count coll)
        obj (clj->js model-data)]
    (aset obj "id" id)
    (aset coll id obj)
    obj))

(deftype LocalStorage []
  IStore
  (-create! [_ collection model-data callback]
    (let [coll  (store-in collection)
          model (add-to-coll coll model-data)]
      (callback nil (js->clj model))))
  
  (-update! [_ collection model cmd callback]
    (let [coll   (store-in collection)
          before (js->clj (aget coll (:id model)))
          f      (cmd/cmd->fn cmd)
          after  (clj->js (f before))]
      (-> (store-in collection)
          (aset (:id model) after))
      (callback nil f)))
  
  (-delete! [_ collection model callback]
    (let [coll (store-in collection)]
      (js-delete coll (:id model))
      (callback nil))))
