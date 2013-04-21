(ns tbn.mutable
  "Contains protocols for mutable objects. This resembles the
   transient collection API (on purpose)"
  (:refer-clojure
   :exclude (-conj! conj! -pop! pop! -assoc! assoc! -dissoc! dissoc! -reset reset!)))

(defprotocol IMCollection
  (-conj! [_ v]))

(defprotocol IMStack
  (-pop! [_]))

(defprotocol IMUpdate
  (-update! [_ cmd]
    "Update a model by supplying a command data
     structure, which describes a change to a an
     extender of this protocol.
     See the command name space for a full documentation
     of the command data structure."))

(defprotocol IMSetable
  (-reset! [_ val]
    "Rplace the contents of the mutable object with 'val'"))

(defprotocol IMAssociative
  (-assoc! [_ k v]))

(defprotocol IMMap
  (-dissoc! [_ k]))

(def conj!   -conj!)
(def pop!    -pop!)
(def update! -update!)
(def reset!  -reset!)
(def assoc!  -assoc!)
(def dissoc! -dissoc!)
