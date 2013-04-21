(ns tbn.events)

(defprotocol IObservable
  (-on      [_ events observer])
  (-off     [_ events observer])
  (-trigger [_ event args]))

(defprotocol IObserver
  (-event [_ args]))

(extend-protocol IObserver
  js/Function
  (-event [f args]
    (apply f args)))

(defn- add-to-channel [channels event observer]
  (update-in channels [event] conj observer))

(defprotocol AddToChannel
  (-add! [events listeners observer]))

(extend-protocol AddToChannel
  PersistentVector
  (-add! [events listeners observer]
    (swap! listeners
           (fn [m]
             (map #(add-to-channel m (key %) observer)
                  (keys m)))))
  js/String
  (-add! [events listeners observer]
    (swap! listeners add-to-channel
           (if (= events :all)
             '_
             events)
           observer)))

(deftype Channels [listeners]
  IObservable
  (-on [_ events observer]
    (-add! events listeners observer))
  (-trigger [_ event args]
    (doseq [listener
            (concat (get @listeners event)
                    (get @listeners '_))]
      (-event listener args))))

(defn channels []
  (Channels. (atom {})))

(defn on
  ([observable observer]
    (-on observable :all observer))
  ([observable events observer]
    (-on observable events observer)))

(defn trigger
  [observable event & args]
  (-trigger observable event args))
