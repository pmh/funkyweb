(ns funkyweb.test.session
  (:use [funkyweb.session] :reload-all)
  (:use [clojure.test]))


(deftest test-session
  (binding [*session* (ref {})]
    (is (= (session assoc :foo "bar" :baz "quux")
           {:foo "bar" :baz "quux"}))
    (is (= (session dissoc :baz)
           {:foo "bar"}))))
