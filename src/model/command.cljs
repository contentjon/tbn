XO(ns model.command)

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
  inc)

(defmethod command->fn :update-in [_ path cmd]
  #(update-in % path (cmd->fn cmd)))

(defn test []
  (assert
   (= (apply-cmd [:inc] 5)
      6))
  (assert
   (= (apply-cmd [:update-in [:foo] [:set 42]] {:foo 4})
      {:foo 42})))
