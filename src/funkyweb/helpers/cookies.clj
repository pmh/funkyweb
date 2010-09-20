(ns funkyweb.helpers.cookies
  (:use [clojure.string :only (split)]))

(declare alter-cookies)

(def *cookies* (ref {}))

(defn cookies-get [key]
  (@*cookies* key))

(defn cookies-set
  ([key val] (cookies-set key val {}))
  ([key val options]
     (alter-cookies assoc key (merge {:value val :path "/"} options))))

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

(defn restore-cookies-from [req]
  (dosync (ref-set *cookies* (:cookies req))))
