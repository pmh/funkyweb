(ns funkyweb.helpers.response)

(declare alter-response)

(def *response* (ref {}))

(defn response
  "Applies f and args to the response map and
  returns a new copy with the changes present

  (response merge {:foo \"bar\"}
    ;=> {:foo \"bar\"}"
  [f & args]
  (apply f @*response* args))

(defn response-set [k v & kvs]
  "Mutates the *response* ref with the result of
  associating the key/value pairs with it

  (response-set :foo \"bar\")
    ;=> {:foo \"bar\"}

  (response-set :baz \"quux\" :quux \"foo\")
    ;=> {:foo \"bar\" :baz \"quux\" :quux \"foo\"}"
  (apply (partial alter-response assoc k v) kvs))

(defn response-get [k]
  "Tries to find the key in the response map
  and returns it if it's presesent otherwise
  it returns nil"
  (@*response* k))

(defn alter-response [f & args]
  "Mutates the *response* ref with the result of
  applying f and args to the response map

  (alter-response assoc :foo \"bar\" :baz \"quux\")
    ;=> {:foo \"bar\" :baz \"quux\"}

  (alter-response dissoc :baz)
    ;=> {:foo \"bar\"}"
  (dosync (apply alter *response* f args)))

(defn reset-response!
  "Resets the *response* ref to {}"
  []
  (dosync (ref-set *response* {})))
