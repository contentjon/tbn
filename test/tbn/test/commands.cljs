(ns tbn.test.commands
  (:require [tbn.commands       :as cmd]
            [cljasmine.checkers :as checkers])
  (:require-macros [cljasmine.macros :as j]))

(j/describe "Commands"
            
  (j/it "support setting of a value"
        
    (j/expect 
      ((cmd/cmd->fn [:set 5]) 3)
      :=
      5))
            
  (j/it "support increasing a value"
        
    (j/expect 
      ((cmd/cmd->fn [:inc]) 8)
      :=
      9))
            
  (j/it "support decreasing a value"
        
    (j/expect
      ((cmd/cmd->fn [:dec]) 3)
      :=
      2))
            
  (j/it "support associcating a value with a key"
        
    (j/expect
      ((cmd/cmd->fn [:assoc :a "foo"]) {})
      :=
      {:a "foo"}))
            
  (j/it "support disassociating a key"
        
    (j/expect
      ((cmd/cmd->fn [:dissoc :bar]) {:bar 1})
      :=
      {}))
  
  (j/it "support updating a nested map"
        
    (j/expect
      ((cmd/cmd->fn [:update-in [:a :b] :set 8]) {:a {:b 1}})
      :=
      {:a {:b 8}})))
