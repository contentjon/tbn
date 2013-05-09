(ns tbn.collection
  (:require [tbn.events  :as e]
            [tbn.model   :as model]
            [tbn.mutable :as m]
            [tbn.store   :as store]))

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
                                this 
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
  
  e/IObservable
  (-on [_ events observer]
    (e/on local events observer)))

(defn cached []
  (-> []
      (vary-meta assoc 
        :channels (e/channels))
      (atom)))

(defn stored [cache store location]
  (StoredCollection. cache store location))
