(ns funkyweb.utils
  (:use [clojure.string :only (blank?)]))

(defn empty-seq? [s] (zero? (count s)))

(defn str-interleave [sep & args]
  (let [args (filter (complement blank?) (map str (flatten args)))]
    (apply str (interleave (repeat sep) args))))

(defn to-keyword [x]
  (keyword (clojure.string/lower-case (str x))))
