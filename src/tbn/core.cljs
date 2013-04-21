(ns tbn.core
  "Core exports of the library"
  (:refer-clojure
   :exclude (conj! pop! assoc! dissoc!))
  (:require [tbn.mutable :as m]
            [tbn.store   :as s]))

;; mutable collection interface

(def conj!   m/conj!)
(def pop!    m/pop!!)
(def update! m/update!)
(def assoc!  m/assoc!)
(def dissoc! m/dissoc!)

;; store interface

(def collection s/collection)
