(ns tbn.mutable
  "Contains protocols for mutable objects. This resembles the
   transient collection API (on purpose)"
  (:refer-clojure
   :exclude (-conj! conj! -pop! pop! -assoc! assoc! -dissoc! dissoc!)))

(defprotocol IMCollection
  (-conj! [_ v]))

(defprotocol IMStack
  (-pop! [_]))

(defprotocol IMUpdate
  (-update! [_ cmd]))

(defprotocol IMSetable
  (-reset! [_ val]))

(defprotocol IMAssociative
  (-assoc! [_ k v]))

(defprotocol IMMap
  (-dissoc! [_ k]))

(def conj!   -conj!)
(def pop!    -pop!)
(def update! -update!)
(def assoc!  -assoc!)
(def dissoc! -dissoc!)
