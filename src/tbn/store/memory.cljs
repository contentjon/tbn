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

  (-create! [a uri model-data callback]
    (let [id         (next-id a)
          model-data (assoc model-data :_id id)
          model      (model/make a uri model-data)]
      ;; add model to store
      (swap! a assoc-in [:store :models id] model)
      (callback nil model)
      a))
  
  (-update! [a collection model cmd callback]
    (let [path      (vector :store :models (:_id @model))
          update-fn (cmds/cmd->fn cmd)]
      (if (identical? (get-in @a path) model)
        (callback nil (update-fn @model))
        (callback (js/Error. "Model" (str path) "does not belong to this store")))
      a))
  
  (-delete! [a collection model callback]
    (let [id   (:_id @model)
          path (vector :store :models)]
      (if-let [stored (contains? (get-in @a path) id)]
        (if-not (identical? stored model)
          (do
            (swap! a update-in path dissoc id)
            (callback nil))
          (callback (js/Error. "Model" (str path) "does not belong to this store")))
        (callback (js/Error. "Model" (str path) "does not exist")))
      a))
  
  (-collection [a uri]
    (when-let [cached (get-in @a [:store :collections uri])]
      (coll/stored cached a uri))))

(defn make []
  (atom {:store {:collections {}
                 :models      {}}
         :id 0}))

(defn add-collection! [a uri]
  (when-not (get-in @a [:store :collections uri])
    (let [coll (coll/cached)]
      (swap! a assoc-in [:store :collections uri] coll))))
