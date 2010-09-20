(ns funkyweb.test.helpers.cookies
  (:use [funkyweb.helpers.cookies] :reload-all)
  (:use [clojure.test]))

(deftest test-cookies-get
  (binding [*cookies* (ref {})]
    (cookies-set :foo "bar")
    (is (= (cookies-get :foo) "bar"))))

(deftest test-cookies-set-with-single-key-value-pair
  (binding [*cookies* (ref {})]
    (cookies-set :foo "bar")
    (is (= @*cookies* {:foo "bar"}))))

(deftest test-cookies-set-with-multiple-key-value-pairs
  (binding [*cookies* (ref {})]
    (cookies-set :foo "bar" :baz "quux" :quux "foo")
    (is (= @*cookies* {:foo "bar" :baz "quux" :quux "foo"}))))

(deftest test-alter-cookies
  (binding [*cookies* (ref {})]
    (is (= (alter-cookies assoc :foo "bar" :baz "quux")
           {:foo "bar" :baz "quux"}))
    (is (= (alter-cookies dissoc :baz)
           {:foo "bar"}))))
