(ns tbn.collection
  "This namespace contains implementations of the following
   set of protocol operations: conj!, pop!, nth count.

   The implemantions model linear collections of models, where
   models can be added and removed with a vector like interface.

   Each collection supports an interface of events that notify
   subsribes on certain occasions:
   :added   A model has been added to a collection
   :removed A model has been removed from a collection
   :reset   The models in a collection have been reset by some
            other event, like a synchronization operation from
            a store"
  (:require [tbn.events  :as e]
            [tbn.model   :as model]
            [tbn.mutable :as m]
            [tbn.store   :as store]))

;;; In memory implementation of a collection used to cache
;;; collections locally

(extend-type Atom

  m/IMCollection
  (-conj! [this model]
    (swap! this conj model)
    (-> @this
        meta
        :channels
        (e/trigger :added model)))

  m/IMStack
  (-pop! [this]
    (let [removed (last @this)]
      (swap! this pop)
      (-> @this
          meta
          :channels
          (e/trigger :removed removed))))

  IIndexed
  (-nth [this n]
    (nth @this n))

  ICounted
  (-count [this]
    (-count @this))

  e/IObservable
  (-on [this events observer]
    (e/on (-> @this
              meta
              :channels)
          events
          observer))

  (-trigger [this event args]
    (-> @this
        meta
        :channels
        (e/-trigger event args))))

;;; A collection implementation that uses another collection as
;;; a cache and propagates changes to a store

(deftype StoredCollection
  [local ;; a local in memory copy of the models
   store ;; an asnychronous store (a remote server or local file store)
   uri   ;; location of the collection within the store
   ]

  m/IMCollection
  (-conj! [this model]
    (store/create! store uri model
      (fn [err model-data]
        (if err
          (e/trigger local :error err)
          (m/-conj! local
                    (model/make store
                                uri
                                model-data))))))

  m/IMStack
  (-pop! [_]
    (store/delete! store uri (:_id @(last @local))
      (fn [err]
        (if err
          (e/trigger local :error err)
          (m/-pop! local)))))

  IIndexed
  (-nth [_ n]
    (-nth local n))

  ICounted
  (-count [_]
    (-count local))

  IDeref
  (-deref [_]
    (map deref @local))

  e/IObservable
  (-on [_ events observer]
    (e/on local events observer)))

(defn cached
  "Returns a collection that is backed by an atom.
   All operations on the collection are therefore only
   in memory and won#t be stored anywhere"
  []
  (-> []
      (vary-meta assoc
        :channels (e/channels))
      (atom)))

(defn stored
  "Creates a collection which is backed by a store.
   All operations on the collection will be propagated
   to the store. Additionally the collection uses a cache,
   which must also implement all collection operations,
   to facilitate local storage of collection data."
  [cache store location]
  (StoredCollection. cache store location))
