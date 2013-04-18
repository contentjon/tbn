(ns tbn.store)

(defprotocol IStore
  (-create! [_ collection model-data callback])
  (-update! [_ collection model cmd callback])
  (-delete! [_ collection model callback]))

(def create! -create!)
(def update! -update!)
(def delete! -delete!)
