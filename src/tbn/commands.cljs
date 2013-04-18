(ns tbn.commands)

(defmulti command->fn
  (fn [key & _]
    key))

(defn cmd->fn [cmd]
  (apply command->fn cmd))

(defn apply-cmd [cmd data]
  (apply (cmd->fn cmd) data))

(defmethod command->fn :set [_ new-value]
  (constantly new-value))

(defmethod command->fn :inc [_]
  inc)

(defmethod command->fn :dec [_]
  dec)

(defmethod command->fn :assoc [_ & kvs]
  #(apply assoc % kvs))

(defmethod command->fn :dissoc [_ & ks]
  #(apply dissoc % ks))

(defmethod command->fn :update-in [_ path cmd & args]
  #(update-in % path (cmd->fn (cons cmd args))))
