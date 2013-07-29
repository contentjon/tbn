(ns tbn.test.store.local
  (:require [async              :as async]
            [cljasmine.checkers :as checkers]
            [tbn.events         :as evt]
            [tbn.store          :as store]
            [tbn.store.local    :as local])
  (:require-macros [cljasmine.macros :as j]))

(def indexed (.-indexedDB js/window))
(def store   nil)

(defn open-db []
  (let [request (.open indexed "unit" 1)]
    (aset request "onerror" 
          (fn [e]
            (throw e)))
    (aset request "onupgradeneeded"
      (fn [e]
        (let [db (aget e "target" "result")]
          (aset (aget e "target" "transaction")
                "onerror"
                #(throw %))
          (when (.contains (aget db "objectStoreNames") "coll")
            (.deleteObjectStore db "coll"))
          (.createObjectStore db "coll" (js-obj "autoIncrement" true)))))
    (aset request "onsuccess"
      (fn [e]
        (->> (aget e "target" "result")
             (local/make)
             (set! store))))))

(defn delete-db [callback]
  (let [request (.deleteDatabase indexed "unit")]
    (doto request
      (aset "onsuccess" callback)
      (aset "onerror"   #(.error js/console "Failed to delete test data base")))))

(defn fresh-db []
  (js/runs 
    (fn []
      (when store
        (.close (.-db store))
        (set! store nil))
      (delete-db open-db)))
  (js/waitsFor
    (fn []
      (not (nil? store)))
    "the database to be opened"
    10000)
  (js/runs
   (fn []
     (j/expect store :not-nil))))

(defn specs []
    
    (j/describe "Creating a store"
      (j/it "makes a new store object"          
        (fresh-db)))
    
    (j/describe "Creating a model"
                      
      (j/it "creates a model in a collection"
  
        (j/waits-for "model to be created" 1000
          :after  (partial store/create! 
                           store 
                           :coll 
                           {:foo "bar"})
          :expect #(j/expect % := {:_id 1 :foo "bar"})))
       
       (j/it "creates distinct ids when creating multiple models"
  
         (j/waits-for "model to be created" 1000
           :after  (partial store/create!
                            store
                            :coll
                            {:baz 8})
           :expect #(j/expect % := {:_id 2 :baz 8}))))
    
    (j/describe "Updating a model"
                
      (j/it "changes the data of the model"
  
        (j/waits-for "model to be updated" 1000
          :after  (partial store/update!
                           store
                           :coll 2
                           [:update-in [:baz] :inc])
          :expect #(j/expect % := {:_id 2 :baz 9}))))
    
    (j/describe "Deleting a model"
      
      (j/it "removes a model from the store"
  
        (j/waits-for "model to be deleted" 1000
          :after  (fn [callback]
                    (store/delete! store :coll 1 #(callback % true)))
          :expect #(j/expect % := true))))
    
    (j/describe "A collection backed by a local store"
              
      (j/it "needs a data base"
        (fresh-db))
              
      (j/it "needs some content"
            
        (j/waits-for "models to be created" 1000
          :after
          (partial js/async.series
                   (array
                     (partial store/create! store :coll {:foo 1})
                     (partial store/create! store :coll {:bar 5})
                     (partial store/create! store :coll {:baz 8})))
          :expect #(j/expect (count %) := 3)))
      
      (j/it "will eventually contain the stored models"
        
        (j/waits-for "models to appear in collection" 1000
          :after
          (fn [callback]
            (let [coll    (local/collection store :coll)
                  counter (atom 0)]
              (evt/on coll :added
                (fn [_]
                  (when (= (swap! counter inc) 3)
                    (callback nil coll))))))
          :expect (fn [coll]
                    (j/expect (count coll) := 3)
                    (j/expect @(nth coll 0) := {:_id 1 :foo 1})
                    (j/expect @(nth coll 1) := {:_id 2 :bar 5})
                    (j/expect @(nth coll 2) := {:_id 3 :baz 8}))))))
