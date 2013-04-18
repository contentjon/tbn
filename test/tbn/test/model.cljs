(ns tbn.test.model
  (:require [tbn.events         :as evt]
            [tbn.model          :as model]
            [tbn.mutable        :as m]
            [tbn.store          :as store]
            [tbn.store.memory   :as mem]
            [cljasmine.checkers :as checkers])
  (:require-macros [cljasmine.macros :as j]))

(j/describe "Models"
            
  (j/it "can be created with initial data"
  
    (let [store (mem/make)]
      
      (store/create! store :coll {:foo "bar"} 
        (fn [err value]
          (j/expect err := nil)
          (let [model (model/make store :coll value)]
            (j/expect @model := {:foo "bar" :_id 1}))))))
            
  (j/it "can be updated"
  
    (let [store (mem/make)]
      
      (store/create! store :coll {:foo 16} 
        (fn [err value]
          (j/expect err := nil)
          (let [model (model/make store :coll value)]
            (j/expect @model := {:foo 16 :_id 1})
            (m/update! model [:update-in [:foo] :inc])
            (j/expect @model := {:foo 17 :_id 1}))))))
            
  (j/it "can change a property explicitly"
  
    (let [store (mem/make)]
      
      (store/create! store :coll {:foo "bar" }
        (fn [err value]
          (j/expect err := nil)
          (let [model (model/make store :coll value)]
            (j/expect @model := {:foo "bar" :_id 1})
            (m/assoc! model :foo "baz")
            (j/expect @model := {:foo "baz" :_id 1}))))))
  
  (j/it "can remove a property entirely"
  
    (let [store (mem/make)]
      
      (store/create! store :coll {:foo "bar" }
        (fn [err value]
          (j/expect err := nil)
          (let [model (model/make store :coll value)]
            (j/expect @model := {:foo "bar" :_id 1})
            (m/dissoc! model :foo)
            (j/expect @model := {:_id 1}))))))
  
  (j/it "emit an event when updated"
        
    (let [store (mem/make)
          diff  (atom nil)]
      
      (store/create! store :coll {:foo "bar"}
        (fn [err value]
          (j/expect err := nil)
          (let [model (model/make store :coll value)]
            (evt/on model :changed #(reset! diff (clojure.data/diff %1 %2)))
            (m/update! model [:assoc :bar "baz"])
            (j/expect 
              @diff
              :=
              [nil {:bar "baz"} {:_id 1 :foo "bar"}])))))))
