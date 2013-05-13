(ns tbn.test.commands
  (:require [tbn.commands       :as cmd]
            [latte.chai         :refer (expect)])
  (:require-macros [latte.core :refer (describe it)]))

(describe "Commands"

  (it "support setting of a value" []

    (expect
      ((cmd/cmd->fn [:set 5]) 3)
      :to.be.equal
      5))

  (it "support increasing a value" []

    (expect
      ((cmd/cmd->fn [:inc]) 8)
      :to.be.equal
      9))

  (it "support decreasing a value" []

    (expect
      ((cmd/cmd->fn [:dec]) 3)
      :to.be.equal
      2))

  (it "support associcating a value with a key" []

    (expect
      ((cmd/cmd->fn [:assoc :a "foo"]) {})
      :to.be.equal
      {:a "foo"}))

  (it "support disassociating a key" []

    (expect
      ((cmd/cmd->fn [:dissoc :bar]) {:bar 1})
      :to.be.equal
      {}))

  (it "support updating a nested map" []

    (expect
      ((cmd/cmd->fn [:update-in [:a :b] :set 8]) {:a {:b 1}})
      :to.be.equal
      {:a {:b 8}})))
