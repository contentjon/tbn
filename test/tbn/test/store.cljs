(ns tbn.test.store
  (:require [tbn.test.store.memory :as memory])
  (:require-macros [cljasmine.macros :as j]))

(j/describe "Stores"

  (j/describe "Simple In Memory Store"
    (memory/specs)))
