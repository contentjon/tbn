(ns tbn.test.collection
  (:require [tbn.collection     :as coll]
            [tbn.events         :as evt]
            [tbn.model          :as model]
            [tbn.mutable        :as m]
            [tbn.store          :as store]
            [tbn.store.memory   :as mem]
            [latte.chai         :refer (expect) :as chai])
  (:require-macros [latte.core :refer (describe it)]))

(chai/plugin "sinon-chai")

(def spies (js/require "sinon"))

(describe "Collections"
            
  (describe "when cached"
    
    (it "are empty when created" []
          
      (-> (coll/cached)
          (count)
          (expect :to.be.equal 0)))
              
    (it "behave like a vector" []
              
      (let [coll (coll/cached)]
          
        (m/conj! coll {:foo "bar"})
          
        (expect (count coll) :to.be.equal 1)
        (expect (nth coll 0) :to.be.equal {:foo "bar"})
        
        (m/conj! coll {:banana "orange"})
        (expect (count coll) :to.be.equal 2)
        (expect (nth coll 1) :to.be.equal {:banana "orange"})
          
        (m/pop! coll)
          
        (expect (count coll) :to.be.equal 1)
        (expect (nth coll 0) :to.be.equal {:foo "bar"})))
              
    (it "emit an event when adding a model" []
          
      (let [coll (coll/cached)]
          
        (evt/on coll :added #(expect % :to.be.equal :a-model))
        (m/conj! coll :a-model)))
              
    (it "emit an event when popping a model" []
          
      (let [coll (coll/cached)]
          
        (evt/on coll :removed #(expect % :to.be.equal :a-model))
        (m/conj! coll :a-model)
        (m/pop! coll))))
   
   (describe "when stored"
                    
     (it "behave like a vector" []
       
       (let [coll (coll/stored (coll/cached) (mem/make) :coll)]
           
         (m/conj! coll {:foo "bar"})
          
         (expect (count coll)  :to.be.equal 1)
         (expect @(nth coll 0) :to.be.equal {:_id 1 :foo "bar"})
        
         (m/conj! coll {:banana "orange"})
         (expect (count coll) :to.be.equal 2)
         (expect @(nth coll 1) :to.be.equal {:_id 2 :banana "orange"})
          
         (m/pop! coll)
          
         (expect (count coll) :to.be.equal 1)
         (expect @(nth coll 0) :to.be.equal {:_id 1 :foo "bar"})))
     
     (it "emit an event when adding a model" []
          
       (let [coll (coll/stored (coll/cached) (mem/make) :coll)
             spy  (.spy spies)]
          
         (evt/on coll :added spy)
         (m/conj! coll {:foo 8})
         (expect spy :to.have.been.calledWith (nth coll 0))))
              
     (it "emit an event when popping a model" []
       
       (let [coll (coll/stored (coll/cached) (mem/make) :coll)
             spy  (.spy spies)]
          
         (evt/on coll :removed spy)
         (m/conj! coll {:foo 8})
         (let [model (nth coll 0)]
           (m/pop! coll)
           (expect spy :to.have.been.calledWith model))))))

