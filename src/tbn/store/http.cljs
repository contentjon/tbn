(ns tbn.store.http
  "A store type that access a web server for storage"
  (:require [tbn.collection :as coll]
            [tbn.commands   :as cmds]
            [tbn.events     :as evt]
            [tbn.model      :as model]
            [tbn.mutable    :as m]
            [tbn.store      :as store]))

(deftype HttpStore [uri]
  
  store/IStore
  
  (-create! [connection collection model-data callback]
    (let [post (XmlHttpRequest.)]
      (.open "POST" (str uri "/" collection))
      (.setRequestHeader "Content-Type" "application/json")
      (aset post "onreadystatechange"
            (fn [s]
              (when (= (aget post "readState") 4)
                (if (= (aget post "status") 201)
                  ;; fetch new instance
                  (let [location (.getRespondeHeader post "Location")
                        get      (XmlHttpRequest.)]
                    (doto get
                      (.open "GET" location)
                      (aset "onreadystatechange"
                            (fn [s]
                              (when (= (aget get "readState") 4)
                                (if (= (aget get "status") 200)
                                  ;; finally notify collection that the
                                  ;; new data has been inserted into
                                  ;; the collection
                                  (callback nil
                                            (-> (aget get "responseText")
                                                (JSON/parse)
                                                (js->clj :keywordize-keys true)))
                                  ;; error during fetch of new data
                                  (-> (str "GET Request failed ("
                                           (aget get "status")
                                           ") !")
                                      (js/Error.)
                                      (callback))))))
                      (.send)))
                  ;; error during create
                  (-> (str "POST Request failed ("
                           (aget post "status")
                           ") !")
                      (js/Error.)
                      (callback))))))
      (.send (JSON/stringify (clj->js model-data)))))
  
  (-update! [a collection model cmd callback]
    ;; not supported yet
    )
  
  (-delete! [a collection model callback]
    ;; not supported yet
    )
  
  (-collection [a collection]
    ;; not supported yet
    ))
