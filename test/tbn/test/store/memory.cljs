(ns tbn.test.store.memory
  (:require [tbn.collection     :as coll]
            [tbn.events         :as evt]
            [tbn.store          :as store]
            [tbn.store.memory   :as mem]
            [latte.chai         :refer (expect)])
  (:require-macros [latte.core :refer (describe it)]))

(defn specs []

  (describe "in memory"
            
    (describe "Creating a model"
      
      (it "Calls the callback with the created model data" []
            
        (-> (mem/make)
            (store/create! :coll {:foo 1}
              (fn [err value]
                (expect err   :to.be.equal nil)
                (expect value :to.be.equal {:foo 1 :_id 1})))))
                
      
      (it "The store contains the models after creation" []
            
        (let [store (mem/make)]
          (store/create! store :coll {:bar "baz"}
            (fn []))
          
          (expect
            (get-in @store [:store :coll 1])
            :to.be.equal
            {:_id 1 :bar "baz"})))
                
      (it "Creating a second model produces a different id" []
            
        (-> (mem/make)
            (store/create! :coll {:foo 42}
              (fn [err value]
                (expect err   :to.be.equal nil)
                (expect value :to.be.equal {:foo 42 :_id 1})))
            (store/create! :coll {:bar 13}
              (fn [err value]
                (expect err   :to.be.equal nil)
                (expect value :to.be.equal {:bar 13 :_id 2}))))))
    
    (describe "Deleting a model"
    
      (it "Removes a model from the store atom" []
            
        (-> (mem/make)
            (store/create! :coll {:foo 1} (fn []))
            (store/delete! :coll 1 
              (fn [err]
                (expect err :to.be.equal nil)))
            (deref)
            (get-in [:store :coll])
            (expect :to.be.equal {})))
                
      (it "Produces an error when deleting a model that does not exist" []
            
        (-> (mem/make)
            (store/create! :coll {:foo 1} (fn []))
            (store/delete! :coll 45
              (fn [err]
                (expect err :not.to.be.null)))
            (deref)
            (get-in [:store :coll 1])
            (expect :to.be.equal {:foo 1 :_id 1}))))
    
    (describe "Updating a model"
                
      (it "changes the contents of an existing model" []
            
        (-> (mem/make)
            (store/create! :coll {:foo 1} (fn []))
            (store/update! :coll 1 [:update-in [:foo] :inc]
              (fn [err value]
                (expect err   :to.be.equal nil)
                (expect value :to.be.equal {:_id 1 :foo 2})))))
                
      (it "produces an error when updating a model that does not exist" []
            
        (-> (mem/make)
            (store/create! :coll {:foo 1} (fn []))
            (store/update! :coll 56 [:update-in [:foo] :inc]
              (fn [err value]
                (expect err :not.to.be.null))))))
                
    (describe "querying for collections"
                  
      (it "support query by collection name" [done]
        
        (let [store (mem/make)
              coll  (store/collection store :fruit)]

          (evt/on coll :reset
            (fn []
              (expect
                (nth coll 0)
                :to.be.equal
                {:type "apple"  :_id 1})
              (expect
                (nth coll 1)
                :to.be.equal
                {:type "orange" :_id 2})
              (done)))
          
          (store/create! store :fruit {:type "apple"}  (fn []))
          (store/create! store :fruit {:type "orange"} (fn [])))))))
