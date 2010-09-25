(ns funkyweb.test.helpers.flash
  (:use [funkyweb.helpers.flash]    :reload-all)
  (:use [funkyweb.helpers.response] :reload-all)
  (:use [funkyweb.helpers.request]  :reload-all)
  (:use [clojure.test]))

(deftest test-flash-set
  (binding [*response* (ref {})]
    (is (= (flash-set :notice "a notice") {:flash {:notice "a notice"}}))
    (is (= (flash-set :notice "a notice" :warning "a warning")
           {:flash {:notice "a notice" :warning "a warning"}}))
    (is (= @*response* {:flash {:foo "bar" :baz "quux"}}))))

(deftest test-flash-set
  (binding [*request* (ref {:flash {:notice "a notice"}})]
    (is (= (flash-get :foo) "a notice"))
    (is (= (flash-get :bar) nil))))
