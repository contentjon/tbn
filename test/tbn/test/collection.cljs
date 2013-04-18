(ns tbn.test.collection
  (:require [tbn.collection     :as coll]
            [tbn.events         :as evt]
            [tbn.model          :as model]
            [tbn.mutable        :as m]
            [tbn.store          :as store]
            [tbn.store.memory   :as mem]
            [cljasmine.checkers :as checkers])
  (:require-macros [cljasmine.macros :as j]))

(j/describe "Collections"
            
  (j/describe "Cached Collections"
    
    (j/it "are empty when created"
          
      (-> (coll/cached)
          (count)
          (j/expect := 0)))
              
    (j/it "behave like a vector"
              
      (let [coll (coll/cached)]
          
        (m/conj! coll {:foo "bar"})
          
        (j/expect (count coll) := 1)
        (j/expect (nth coll 0) := {:foo "bar"})
        
        (m/conj! coll {:banana "orange"})
        (j/expect (count coll) := 2)
        (j/expect (nth coll 1) := {:banana "orange"})
          
        (m/pop! coll)
          
        (j/expect (count coll) := 1)
        (j/expect (nth coll 0) := {:foo "bar"})))
              
    (j/it "emit an event when adding a model"
          
      (let [coll (coll/cached)]
          
        (evt/on coll :added #(j/expect % := :a-model))
        (m/conj! coll :a-model)))
              
    (j/it "emit an event when popping a model"
          
      (let [coll (coll/cached)]
          
        (evt/on coll :removed #(j/expect % := :a-model))
        (m/conj! coll :a-model)
        (m/pop! coll))))
   
   (j/describe "Stored Collections"
                    
     (j/it "behave like a vector"
       
       (let [coll (coll/stored (coll/cached) (mem/make) :coll)]
           
         (m/conj! coll {:foo "bar"})
          
         (j/expect (count coll)  := 1)
         (j/expect @(nth coll 0) := {:_id 1 :foo "bar"})
        
         (m/conj! coll {:banana "orange"})
         (j/expect (count coll) := 2)
         (j/expect @(nth coll 1) := {:_id 2 :banana "orange"})
          
         (m/pop! coll)
          
         (j/expect (count coll) := 1)
         (j/expect @(nth coll 0) := {:_id 1 :foo "bar"})))
               
     (j/it "emit an event when adding a model"
          
       (let [coll (coll/stored (coll/cached) (mem/make) :coll)
             spy  (js/jasmine.createSpy "callback")]
          
         (evt/on coll :added spy)
         (m/conj! coll {:foo 8})
         (j/expect spy :to-have-been-called-with (nth coll 0))))
              
     (j/it "emit an event when popping a model"
          
       (let [coll (coll/stored (coll/cached) (mem/make) :coll)
             spy  (js/jasmine.createSpy "callback")]
          
         (evt/on coll :removed spy)
         (m/conj! coll {:foo 8})
         (let [model (nth coll 0)]
           (m/pop! coll)
           (j/expect spy :to-have-been-called-with model))))))
