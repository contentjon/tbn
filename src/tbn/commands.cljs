(ns tbn.commands
  "The command data structure represents a serialized
   version of functions that can be executed on models.
   They are used to transmit model operations between
   clients and servers.")

(defmulti command->fn
  "Compiles a command name and parameters into
   a function"
  (fn [key & _]
    key))

(defn cmd->fn
  "Take a command data structure and returns
   a function that performs the operations
   represented by this command"
  [cmd]
  (apply command->fn cmd))

(defn apply-cmd
  "Compile a command into a function and applies
   the command to 'data'"
  [cmd data]
  (apply (cmd->fn cmd) data))

;;; Standard command implementations

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
