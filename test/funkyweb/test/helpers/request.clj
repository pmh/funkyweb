(ns funkyweb.test.helpers.request
  (:use [funkyweb.helpers.request] :reload-all)
  (:use [clojure.test]))

(deftest test-request-get
  (binding [*request* (ref {:foo "bar"})]
    (is (= (request-get :foo) "bar"))))
