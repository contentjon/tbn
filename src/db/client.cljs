(ns db.client
  (:require [clojure.string :as str]
            [db.core :as db]
            [db.json :as json]))

(defn log [& msg]
  (->> msg
       (apply println-str)
       (.log js/console)))

(defn xhr [method url body callback]
  (let [x    (js/XMLHttpRequest.)]
    (.open x method url)
    (set! (.-onreadystatechange x)
          (fn [s]
            (when (= 4 (.-readyState x))
              (callback (.-responseText x)))))
    (if body
      (do
        (.setRequestHeader x "Content-Type" "application/json")
        (.send x body))
      (.send x))))

(defn url-cat [base ext]
  (str (name base) "/" (name ext)))

(defrecord DBClient [server-url]
  db/AsyncDB
  (-read
   [a key callback]
   (xhr "GET" (url-cat server-url key) nil callback))
  (-write
   [a key value callback]
   (xhr "PUT" (url-cat server-url key) value callback))
  (-delete
   [a key callback]
   (xhr "DELETE" (url-cat server-url key) nil callback)))

(def make (comp json/wrap ->DBClient))
