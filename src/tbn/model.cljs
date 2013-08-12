(ns tbn.model
  "A model is a mutable map data structure. Models are contained
   in collections are instanciated from a data store or added by
   client code for persistence.

   They minimally support the following protocol
   methods: update!, set!, assoc!, reset!, assoc!, dissoc!, on, deref

   Models trigger events on certain occasions:
   :changed The data of the model has been changed either because client
            code has called one of the protocol methods that modify the
            model or because data was synced with a store. This is event
            is not triggered during the initial instatiation of the model
            from a store. Listen to the :added and :reset events of a
            collection to get notifications about model creation."
  (:require [tbn.events   :as e]
            [tbn.mutable  :as m]
            [tbn.store    :as store]))

;;; Model implementation that propagates changes to a store

(deftype Model
  [local
   store
   collection
   channels]

  m/IMUpdate
  (-update! [this cmd]
    (store/update! store collection this cmd
      (fn [err updated]
        (if err
          (e/trigger local :error err)
          (let [current @local
                new     (reset! local updated)]
            (e/trigger channels :changed current new))))))
  
  m/IMSetable
  (-reset! [this val]
    (m/-update! this [:set val]))

  m/IMAssociative
  (-assoc! [this k v]
    (m/-update! this [:assoc k v]))

  m/IMMap
  (-dissoc! [this k]
    (m/-update! this [:dissoc k]))

  e/IObservable
  (-on [_ events observer]
    (e/on channels events observer))

  IDeref
  (-deref [_]
    @local))

(defn make
  "Create a model that is backed by a store and to be added
   to 'collection'. The model intially contains 'data'.

   The model contents are cached in an atom and also propagated
   to the underyling store implementation."
  [store collection data]
  (Model. (atom data) store collection (e/channels)))
