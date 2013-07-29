(ns tbn.store.local
  "Implements storage using the HTML5 indexed DB API"
  (:require [async          :as async]
            [tbn.collection :as coll]
            [tbn.events     :as evt]
            [tbn.model      :as model]
            [tbn.mutable    :as m]
            [tbn.store      :as store]
            [tbn.commands   :as cmds]))

(def ro "readonly")
(def rw "readwrite")

(def noop        (fn []))
(def passthrough (fn [a b] a))
(def reset       (fn [a b] b))

(def deserialize #(js->clj % :keywordize-keys true))
(def serialize   clj->js)

(defn- assoc-id [data id]
  (assoc data :_id id))

(defn- reserved-wrapper
  "Workaround for a closure compiler bug, which doesn't accept
   the reserved keywords as a method name in some cases"
  [method this & args]
  (let [clazz (type this)]
    (.apply (aget clazz "prototype" method)
            this
            (into-array args))))

(def delete   (partial reserved-wrapper "delete"))
(def continue (partial reserved-wrapper "continue"))

(defn cursor [db collection f callback]
  (try
    (let [collection  (name collection)
          transaction (.transaction (.-db db) (array collection))
          store       (.objectStore transaction collection)
          request     (.openCursor store)]
      (aset request "onsuccess"
            (fn [e]
              (when-let [cursor (aget e "target" "result")]
                (f cursor)
                (continue cursor))))
      (doto transaction
        (aset "onerror"
              #(callback (aget % "error")))
        (aset "oncomplete"
              #(callback (aget % "target" "result")))))
    (catch js/DOMException e
      (.error js/console e)
      (callback e))))

(defn- add-model [store collection cursor]
  (->> (assoc-id (deserialize (.-value cursor)) (.-key cursor))
       (model/make store collection)
       (m/conj! collection)))

(defn collection [store collection]
  (let [coll (coll/cached)]
    (cursor store collection
      (partial add-model store coll)
      #(evt/trigger coll :reset))
    (coll/stored coll store collection)))

(defn- transaction [db collection callback & fns]
  (try
    (let [collection  (name collection)
          transaction (.transaction db (array collection) rw)
          store       (.objectStore transaction collection)]
      ((apply js/async.compose
        (into-array (reverse fns)))
        nil
        store
        callback))
    (catch js/DOMException e
      (.error js/console e)
      (callback e))))

(defn- request 
  ([f result-fn]
   (fn [prev store callback]
     (let [result-fn (or result-fn reset)
           request   (f prev store)]
       (doto request
         (aset "onerror"
               #(callback (aget % "error")))
         (aset "onsuccess"
               (fn [e]
                 (callback nil
                           (->> (aget e "target" "result")
                                (result-fn prev)) 
                           store)))))))
  ([f]
   (request f nil)))

(defn- getter [id result-fn]
  (request
    #(.get %2 id)
    result-fn))

(defn- adder 
  ([data result-fn]
    (request
      #(.add %2 (serialize data))
      result-fn))
  ([result-fn]
    (request
      #(.add %2 (serialize %1))
      result-fn)))

(defn- putter [id result-fn]
  (request
    #(.put %2 (serialize %1) id)
    result-fn))

(defn- deleter [id]
  (request #(delete %2 id)))

(defn- immediately [f]
  (fn [prev store callback]
    (callback nil (f prev) store)))

(deftype IndexedDBStorage [db]
  
  store/IStore
  
  (-create! [_ collection model-data callback]
    (transaction db collection callback
      (immediately (constantly model-data))
      (adder assoc-id)))
  
  (-update! [_ collection model cmd callback]
    (let [update-fn (cmds/cmd->fn cmd)]
      (transaction db collection callback
        (getter model (comp update-fn deserialize reset))
        (putter model assoc-id))))
  
  (-delete! [_ collection model callback]
    (transaction db collection callback
      (deleter model))))

(defn make 
  ([storage]
    (IndexedDBStorage. storage))
  ([database version migrations callback]
    (let [request (.open (.-indexedDB js/window) database version)]
      (aset request "onerror" callback)
      (aset request 
            "onsuccess"
            (fn [e]
              (->> (aget e "target" "result")
                   (make)
                   (callback nil))))
      (aset request
            "onupgradeneeded"
            (fn [e]
              (let [db         (aget e "target" "result")
                    old        (aget e "oldVersion")
                    new        (aget e "newVersion")
                    migrations (take (- new old) (drop old migrations))]
                (aset (aget e "target" "transaction")
                      "onerror"
                      #(throw %))
                (doseq [migration migrations]
                  (migration db))))))))

(defn add-collection [db collection-key]
  (.createObjectStore db (name collection-key) (js-obj "autoIncrement" true)))
