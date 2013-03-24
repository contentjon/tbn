(ns db.core
  (:refer-clojure :exclude [-write replace]))

(defprotocol IStore
  (-create! [_ collection model-data callback])  
  (-update! [_ collection model cmd callback])
  (-delete! [_ collection model callback]))

(def create! -create!)
(def update! -update!)
(def delete! -delete!)

;; (defprotocol AsyncDB
;;   (-read    [db key callback])
;;   (-write   [db key value callback])
;;   )

;; (defn error [& msg]
;;   (throw (apply println-str msg)))

;; (def not-found ::not-found)

;; (defn not-found? [x]
;;   (= x not-found))

;; (defn read [db key callback]
;;   (-read db key
;;          (fn [x]
;;            (when (not-found? x)
;;              (error "read on not-existing key:"))
;;            (callback x))))

;; (defn exists? [db key callback]
;;   (-read db key
;;          (fn [x]
;;            (callback (not (not-found? x))))))

;; (defn create [db key value callback]
;;   (exists? db key
;;            (fn [exist?]
;;              (when exist?
;;                (error "create already existing key:" key))
;;              (-write db key value callback))))

;; (defn replace [db key value callback]
;;   (exists? db key
;;            (fn [exist?]
;;              (when-not exist?
;;                (error "replace non-existing key:" key))
;;              (-write db key value callback))))

;; (defn update [db key f callback]
;;   (read db key
;;         (fn [x]
;;           (replace db key (f x) callback))))

;; (defn delete [db key callback]
;;   (exists? db key
;;            (fn [exist?]
;;              (when-not exist?
;;                (error "delete non-existing key:" key))
;;              (-delete db key callback))))
