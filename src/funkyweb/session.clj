(ns funkyweb.session)

(declare alter-session)

(def *session* (ref {}))

(defn session-get [key]
  (@*session* key))

(defn session-set [key val & kvs]
  (apply (partial alter-session assoc key val) kvs))

(defn alter-session
  "Mutates the *session* ref with the result of
  applying args to f

  (session assoc :foo \"bar\" :baz \"quux\")
    ;=> {:foo \"bar\" :baz \"quux\"}

  (session dissoc :baz)
    ;=> {:foo \"bar\"}"
  [f & args]
  (dosync
   (apply alter *session* f args)))
