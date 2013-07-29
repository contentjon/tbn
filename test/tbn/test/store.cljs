(ns tbn.test.store
  (:require [tbn.test.store.local  :as local]
            [tbn.test.store.memory :as memory])
  (:require-macros [latte.core :refer (describe)]))

(describe "Stores"

  (j/describe "IndexedDB store"
    (local/specs))

  (j/describe "Simple In Memory Store"
    (memory/specs)))

