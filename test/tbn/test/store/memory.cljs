(ns tbn.test.store.memory
  (:require [tbn.collection     :as coll]
            [tbn.events         :as evt]
            [tbn.store          :as store]
            [tbn.store.memory   :as mem]
            [cljasmine.checkers :as checkers])
  (:require-macros [cljasmine.macros :as j]))


(defn specs []

  (j/describe "In memory stores"
            
    (j/describe "Creating a model"
      
      (j/it "Calls the callback with the created model data"
            
        (-> (mem/make)
            (store/create! :coll {:foo 1}
              (fn [err value]
                (j/expect err   := nil)
                (j/expect value := {:foo 1 :_id 1})))))
                
      
      (j/it "The store contains the models after creation"
            
        (let [store (mem/make)]
          (store/create! store :coll {:bar "baz"}
            (fn []))
          
          (j/expect
            (get-in @store [:store :coll 1])
            :=
            {:_id 1 :bar "baz"})))
                
      (j/it "Creating a second model produces a different id"
            
        (-> (mem/make)
            (store/create! :coll {:foo 42}
              (fn [err value]
                (j/expect err   := nil)
                (j/expect value := {:foo 42 :_id 1})))
            (store/create! :coll {:bar 13}
              (fn [err value]
                (j/expect err   := nil)
                (j/expect value := {:bar 13 :_id 2}))))))
    
    (j/describe "Deleting a model"
    
      (j/it "Removes a model from the store atom"
            
        (-> (mem/make)
            (store/create! :coll {:foo 1} (fn []))
            (store/delete! :coll 1 
              (fn [err]
                (j/expect err := nil)))
            (deref)
            (get-in [:store :coll])
            (j/expect := {})))
                
      (j/it "Produces an error when deleting a model that does not exist"
            
        (-> (mem/make)
            (store/create! :coll {:foo 1} (fn []))
            (store/delete! :coll 45
              (fn [err]
                (j/expect err :not-nil)))
            (deref)
            (get-in [:store :coll 1])
            (j/expect := {:foo 1 :_id 1}))))
              
    (j/describe "Updating a model"
                
      (j/it "changes the contents of an existing model"
            
        (-> (mem/make)
            (store/create! :coll {:foo 1} (fn []))
            (store/update! :coll 1 [:update-in [:foo] :inc]
              (fn [err value]
                (j/expect err   := nil)
                (j/expect value := {:_id 1 :foo 2})))))
                
      (j/it "produces an error when updating a model that does not exist"
            
        (-> (mem/make)
            (store/create! :coll {:foo 1} (fn []))
            (store/update! :coll 56 [:update-in [:foo] :inc]
              (fn [err value]
                (j/expect err :not-nil))))))
                
    (j/describe "Querying for collections"
                  
      (j/it "supports query by collection name"
           
        (let [store (mem/make)]
          
          (store/create! store :fruit {:type "apple"}  (fn []))
          (store/create! store :fruit {:type "orange"} (fn []))
        
          (j/waits-for "models to appear in collection" 500
            :after  (fn [done]
                      (let [coll (store/collection store :fruit)]
                        (evt/on coll :reset #(done nil coll))))
            :expect (fn [coll]
                      (j/expect
                        (nth coll 0)
                        :=
                        {:type "apple"  :_id 1})
                      (j/expect
                        (nth coll 1)
                        :=
                        {:type "orange" :_id 2}))))))))
