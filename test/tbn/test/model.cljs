(ns tbn.test.model
  (:require [clojure.data       :as data]
            [tbn.events         :as evt]
            [tbn.model          :as model]
            [tbn.mutable        :as m]
            [tbn.store          :as store]
            [tbn.store.memory   :as mem]
            [latte.chai         :refer (expect)])
  (:require-macros [latte.core :refer (describe it)]))

(describe "Models"
            
  (it "can be created with initial data" []
  
    (let [store (mem/make)]
      
      (store/create! store :coll {:foo "bar"} 
        (fn [err model]
          (expect err :to.be.equal nil)
          (expect @model :to.be.equal {:foo "bar" :_id 1})))))
  
  (it "can be updated" []
  
    (let [store (mem/make)]
      
      (store/create! store :coll {:foo 16} 
        (fn [err model]
          (expect err :to.be.equal nil)
          (expect @model :to.be.equal {:foo 16 :_id 1})
          (m/update! model [:update-in [:foo] :inc])
          (expect @model :to.be.equal {:foo 17 :_id 1})))))
            
  (it "can change a property explicitly" []
  
    (let [store (mem/make)]
      
      (store/create! store :coll {:foo "bar" }
        (fn [err model]
          (expect err :to.be.equal nil)
          (expect @model :to.be.equal {:foo "bar" :_id 1})
          (m/assoc! model :foo "baz")
          (expect @model :to.be.equal {:foo "baz" :_id 1})))))
  
  (it "can remove a property entirely" []
  
    (let [store (mem/make)]
      
      (store/create! store :coll {:foo "bar" }
        (fn [err model]
          (expect err :to.be.equal nil)
          (expect @model :to.be.equal {:foo "bar" :_id 1})
          (m/dissoc! model :foo)
          (expect @model :to.be.equal {:_id 1})))))
  
  (it "emit an event when updated" []
        
    (let [store (mem/make)
          diff  (atom nil)]
      
      (store/create! store :coll {:foo "bar"}
        (fn [err model]
          (expect err :to.be.equal nil)
          (evt/on model :changed #(reset! diff (data/diff %1 %2)))
          (m/update! model [:assoc :bar "baz"])
          (expect 
           @diff
           :to.be.equal
           [nil {:bar "baz"} {:_id 1 :foo "bar"}]))))))
