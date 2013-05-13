(ns tbn.store
  "The protocols in this name space abstract the storage
   medium for collections and models. When collections
   and models are backed by a store, they use this
   interface to persist their data.")

(defprotocol IStore
  (-create! [_ collection model-data callback]
    "Create a model in a collection")
  (-update! [_ collection model cmd callback]
    "Update the contents of a model with a command")
  (-delete! [_ collection model callback]
    "Delete a model from a collection")
  (-collection [_ uri]
    "Instantiate a new collection from a store"))

(def create!    -create!)
(def update!    -update!)
(def delete!    -delete!)
(def collection -collection)
