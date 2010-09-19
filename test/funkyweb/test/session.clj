(ns funkyweb.test.session
  (:use [funkyweb.session] :reload-all)
  (:use [clojure.test]))

(deftest test-session-get
  (binding [*session* (ref {})]
    (session-set :foo "bar")
    (is (= (session-get :foo) "bar"))))

(deftest test-session-set-with-single-key-value-pair
  (binding [*session* (ref {})]
    (session-set :foo "bar")
    (is (= @*session* {:foo "bar"}))))

(deftest test-session-set-with-multiple-key-value-pairs
  (binding [*session* (ref {})]
    (session-set :foo "bar" :baz "quux" :quux "foo")
    (is (= @*session* {:foo "bar" :baz "quux" :quux "foo"}))))

(deftest test-alter-session
  (binding [*session* (ref {})]
    (is (= (alter-session assoc :foo "bar" :baz "quux")
           {:foo "bar" :baz "quux"}))
    (is (= (alter-session dissoc :baz)
           {:foo "bar"}))))
