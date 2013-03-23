(ns model.core
  (:refer-clojure :exclude (-conj! -pop! -assoc! -dissoc!)))

(defprotocol IMCollection
  (-conj! [_ v]))

(defprotocol IMStack
  (-pop! [_]))

(defprotocol IMAssociative
  (-assoc! [_ k v]))

(defprotocol IMMap
  (-dissoc! [_ k]))

(deftype Model
  [local
   store]

  IMAssociative
  (-assoc! [_ k v]
    (store/update store
      (swap! @local -assoc k v)))

  IMMap
  (-dissoc! [_ k v]
    (swap! @local -dissoc k v))

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
      (fn [err model]
        (if err
          (.error js/console "Couldn't create model")
          ;; TODO fire error event
          (store/swap! local conj model)
          ;; TODO fire create event
          ))))

  IMStack
  (-pop! [_]
    (when-let [model (swap! local pop)]
      (store/delete store uri model
        (fn [err]
          ;; TODO: fire remove event
          ))))

  ILookup
  (-lookup
    ([this k]
      (-lookup this k nil))
    ([_ k not-found]
     ;; TODO: provide lookup by id
     not-found))

  IIndexed
  (-nth [_ n]
    (nth @local n)))
