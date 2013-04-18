(ns tbn.model
  (:require [tbn.events   :as e]
            [tbn.mutable  :as m]
            [tbn.store    :as store]))

(deftype Model
  [local
   store
   collection
   channels]
  
  m/IMUpdate
  (-update! [_ cmd]
    (store/update! store collection (:_id @local) cmd
      (fn [err updated]
        (if err
          (e/trigger local :error err)
          (let [current @local
                new     (reset! local updated)]
            (e/trigger channels :changed current new))))))
  
  m/IMSetable
  (-set! [this val]
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

(defn make [store collection data]
  (Model. (atom data) store collection (e/channels)))
