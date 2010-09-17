(ns funkyweb.session)

(def *session* (ref {}))

(defn session
  "Mutates the *session* ref with the result of
  applying args to f

  (session assoc :foo \"bar\" :baz \"quux\")
    ;=> {:foo \"bar\" :baz \"quux\"}

  (session dissoc :baz)
    ;=> {:foo \"bar\"}"
  [f & args]
  (dosync
   (apply alter *session* f args)))
