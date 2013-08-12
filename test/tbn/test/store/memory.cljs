(ns tbn.test.store.memory
  (:require [tbn.collection     :as coll]
            [tbn.events         :as evt]
            [tbn.store          :as store]
            [tbn.store.memory   :as mem]
            [latte.chai         :refer (expect)])
  (:require-macros [latte.core :refer (describe it)]))

(def async (js/require "async"))

(defn specs []

  (describe "in memory"
            
    (describe "Creating a model"
      
      (it "Calls the callback with the created model" []

        (let [store (mem/make)
              _     (mem/add-collection! store :coll)]
          
          (-> store
              (store/create! :coll {:foo 1}
                (fn [err value]
                  (expect err    :to.be.equal nil)
                  (expect @value :to.be.equal {:foo 1 :_id 1}))))))
                
      
      (it "The store contains the models after creation" []
            
        (let [store (mem/make)
              _     (mem/add-collection! store :coll)]
          
          (store/create! store :coll {:bar "baz"}
            (fn []))
          
          (expect
            @(get-in @store [:store :models 1])
            :to.be.equal
            {:_id 1 :bar "baz"})))
      
      (it "Creating a second model produces a different id" []

        (let [store (mem/make)
              _     (mem/add-collection! store :coll)]
          (-> store
              (store/create! :coll {:foo 42}
                (fn [err value]
                  (expect err   :to.be.equal nil)
                  (expect @value :to.be.equal {:foo 42 :_id 1})))
              (store/create! :coll {:bar 13}
                (fn [err value]
                  (expect err   :to.be.equal nil)
                  (expect @value :to.be.equal {:bar 13 :_id 2})))))))
    
    (describe "Deleting a model"
    
      (it "Removes a model from the store atom" [done]

        (let [store (mem/make)
              _     (mem/add-collection! store :coll)]

          (.waterfall async
            (array (partial store/create! store :coll {:foo 1})
                   (partial store/delete! store :coll)
                   (fn [callback]
                     (-> @store
                         (get-in [:store :models 1])
                         (expect :to.be.equal nil))
                     (callback nil)))
            done)))
      
      (it "Produces an error when deleting a model that does not exist" []

        (let [store (mem/make)
              _     (mem/add-collection! store :coll)]
          
          (store/create! store :coll {:foo 1}
            (fn [err model]
              (store/delete! store :coll (atom {:_id 45})
                (fn [err]
                  (expect err :not.to.be.null)
                  (-> @store
                      (get-in [:store :models 1])
                      (expect :to.be.equal {:foo 1 :_id 1})))))))))
    
    (describe "Updating a model"
                
      (it "changes the contents of an existing model" []

        (let [store (mem/make)
              _     (mem/add-collection! store :coll)]
          
          (store/create! store :coll {:foo 1}
            (fn [err model]
              (store/update! store :coll model [:update-in [:foo] :inc]
                (fn [err value]
                  (expect err   :to.be.equal nil)
                  (expect value :to.be.equal {:_id 1 :foo 2})))))))
      
      (it "produces an error when updating a model that does not exist" []

        (let [store (mem/make)
              _     (mem/add-collection! store :coll)]
          
          (store/create! store :coll {:foo 1}
            (fn [err model]
              (store/update! :coll (atom {:_id 45}) [:update-in [:foo] :inc]
                (fn [err]
                  (.log js/console "TEST")
                  (expect err :not.to.be.null)
                  ;; (-> @store
                  ;;     (get-in [:store :models 1])
                  ;;     (expect :to.be.equal {:foo 1 :_id 1}))
                  )))))))
    
    (describe "querying for collections"
                  
      (it "support query by collection name" [done]

        (let [store (mem/make)
              _     (mem/add-collection! store :fruit)
              coll  (store/collection store :fruit)]

          (m/conj! coll {:type "apple"})
          (m/conj! coll {:type "orange"})

          (let [coll (store/collection store :fruit)]
            (expect
              @(nth coll 0)
              :to.be.equal
              {:type "apple"  :_id 1})
            (expect
              @(nth coll 1)
              :to.be.equal
              {:type "orange" :_id 2})))))))
