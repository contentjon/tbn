(ns tbn.test.store
  (:require [tbn.test.store.memory :as memory])
  (:require-macros [latte.core :refer (describe)]))

(describe "Stores"

  (memory/specs))
