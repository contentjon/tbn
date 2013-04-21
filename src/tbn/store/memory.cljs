(ns tbn.store.memory
  "This type of store keeps all data in memory and
   exists mostly for testing purposes.
   Collections of models are indexed by regular clojure
   values, like any map. Models indices are automatically
   generated and increase monotonically"
  (:require [tbn.collection :as coll]
            [tbn.commands   :as cmds]
            [tbn.events     :as evt]
            [tbn.model      :as model]
            [tbn.mutable    :as m]
            [tbn.store      :as store]))

(defn- next-id [a]
  (:id (swap! a update-in [:id] inc)))

(defn- ->models [a coll]
  (->> (get-in @a [:store coll])
       (sort-by key)))

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
      a))
  
  (-collection [a collection]
    (let [cached (coll/cached)]
      (js/setTimeout 
        (fn []
          (doseq [model (->models a collection)]
            (m/conj! cached (val model)))
          (evt/trigger cached :reset))
        0)
      (coll/stored cached a collection))))

(defn make []
  (atom { :store {} :id 0 }))
