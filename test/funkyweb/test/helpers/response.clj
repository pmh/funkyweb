(ns funkyweb.test.helpers.response
  (:use [funkyweb.helpers.response] :reload-all)
  (:use [clojure.test]))

(deftest test-response
  (binding [*response* (ref {})]
    (is (= (response merge {:foo "bar"}) {:foo "bar"}))
    (is (= @*response* {}))))

(deftest test-response-get
  (binding [*response* (ref {})]
    (response-set :foo "bar")
    (is (= (response-get :foo) "bar"))))

(deftest test-response-set
  (binding [*response* (ref {})]
    (response-set :foo "bar")
    (is (= @*response* {:foo "bar"}))))

(deftest test-alter-response
  (binding [*response* (ref {})]
    (is (= (alter-response assoc :foo "bar" :baz "quux")
           {:foo "bar" :baz "quux"}))
    (is (= (alter-response dissoc :baz)
           {:foo "bar"}))))

(deftest test-reset-response
  (binding [*response* (ref {:foo "bar" :baz "quux"})]
    (reset-response!)
    (is (= @*response* {}))))

