(ns tbn.store.memory
  (:require [tbn.collection :as coll]
            [tbn.commands   :as cmds]
            [tbn.events     :as evt]
            [tbn.model      :as model]
            [tbn.mutable    :as m]
            [tbn.store      :as store]))

(defn- next-id [a]
  (:id (swap! a update-in [:id] inc)))

(extend-protocol store/IStore
  cljs.core.Atom
  
  (-create! [a collection model-data callback]
    (let [id         (next-id a)
          model-data (assoc model-data :_id id)]
      (swap! a assoc-in [:store collection id] model-data)
      (callback nil model-data)
      a))
  
  (-update! [a collection model cmd callback]
    (let [path      (vector :store collection model)
          update-fn (cmds/cmd->fn cmd)]
      (if (get-in @a path)
        (let [updated (swap! a update-in path update-fn)]
          (->> (get-in updated path)
               (callback nil)))
        (callback (js/Error. "Model" (str path) "does not exist")))
      a))
  
  (-delete! [a collection model callback]
    (let [path (vector :store collection)]
      (if-not (contains? (get-in @a path) model)
        (callback (js/Error. "Model" (str path) "does not exist"))
        (do
          (swap! a update-in path dissoc model)
          (callback nil)))
      a)))

(defn make []
  (atom { :store {} :id 0 }))

(defn- ->models [a coll]
  (->> (get-in @a [:store coll])
       (sort-by key)))

(defn collection [a coll]
  (let [cached (coll/cached)]
    (js/setTimeout 
     (fn []
       (doseq [model (->models a coll)]
         (m/conj! cached (val model)))
       (evt/trigger cached :reset))
     0)
    (coll/stored cached a coll)))
