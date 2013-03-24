(ns model.core
  (:refer-clojure :exclude (-conj! -pop! -assoc! -dissoc! -update!))
  (:require [model.command :as cmd]))

(defprotocol IMCollection
  (-conj! [_ v]))

(defprotocol IMStack
  (-pop! [_]))

(defprotocol IMSetable
  (-reset! [_ val]))

(defprotocol IMAssociative
  (-assoc! [_ k v]))

(defprotocol IMMap
  (-dissoc! [_ k]))

(deftype Model
  [local
   store
   uri]
  
  IMUpdate
  (-update! [_ cmd]
    (store/update! store uri @local
      (fn [err update-fn]
        (if err
          (.error js/console "Couldn't change model with operation:" cmd)
          ;; TODO trigger error event
          (swap! local update-fn)
          ;; TODO trigger change event
          ))))
  
  IMSetable
  (-set! [this val]
    (-update! this [:set val]))
  
  IMAssociative
  (-assoc! [this k v]
    (-update! this [:assoc k v]))
  
  IMMap
  (-dissoc! [_ k v]
    (-update! this [:dissoc k v]))
  
  ILookup
  (-lookup
    ([_ k]
      (-lookup @local k))
    ([_ k not-found]
      (-lookup @local k not-found))))

(deftype ModelCollection
  [local ;; a local in memory copy of the model
   store ;; an asnychronous store (a remote server or local file store)
   uri   ;; location of the collection within the store
   ]
  
  IMCollection
  (-conj! [_ model]
    (store/create store uri model
      (fn [err model-data]
        (if err
          (.error js/console "Couldn't create model")
          ;; TODO fire error event
          (store/swap! local conj (Model. (atom model-data) store uri))
          ;; TODO fire create event
          ))))
  
  IMStack
  (-pop! [_]
    (store/delete store uri (last @local)
      (fn [err]
        (if error
          (.error js/console "Couldn't create model")
          ;; TODO: fire error event
          (swap! local pop)
          ;; TODO: fire remove event
          )
        )))
  
  IIndexed
  (-nth [_ n]
    (nth @local n)))
