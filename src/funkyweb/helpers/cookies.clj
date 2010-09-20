(ns funkyweb.helpers.cookies)

(declare alter-cookies)

(def *cookies* (ref {}))

(defn cookies-get [key]
  (@*cookies* key))

(defn cookies-set [key val & kvs]
  (apply (partial alter-cookies assoc key val) kvs))

(defn alter-cookies
  "Mutates the *cookies* ref with the result of
  applying args to f

  (alter-cookies assoc :foo \"bar\" :baz \"quux\")
    ;=> {:foo \"bar\" :baz \"quux\"}

  (alter-cookies dissoc :baz)
    ;=> {:foo \"bar\"}"
  [f & args]
  (dosync
   (apply alter *cookies* f args)))

(defn restore-cookies-from [new-value]
  (dosync (ref-set *cookies* new-value)))
