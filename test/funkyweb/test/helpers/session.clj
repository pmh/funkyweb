(ns funkyweb.test.helpers.session
  (:use [funkyweb.helpers.session]  :reload-all)
  (:use [funkyweb.helpers.response] :reload-all)
  (:use [funkyweb.helpers.request]  :reload-all)
  (:use [clojure.test]))

(deftest test-session-set
  (binding [*response* (ref {})]
    (is (= (session-set :foo "bar") {:session {:foo "bar"}}))
    (is (= (session-set :foo "bar" :baz "quux")
           {:session {:foo "bar" :baz "quux"}}))
    (is (= @*response* {:session {:foo "bar" :baz "quux"}}))))

(deftest test-session-get
  (binding [*request* (ref {:session {:foo "bar"}})]
    (is (= (session-get :foo) "bar"))
    (is (= (session-get :baz) nil))))
