(ns funkyweb.test.helpers.cookies
  (:use [funkyweb.helpers.cookies] :reload-all)
  (:use [funkyweb.helpers.response] :reload-all)
  (:use [funkyweb.helpers.request]  :reload-all)
  (:use [clojure.test]))

(deftest test-cookies-get
  (binding [*request* (ref {:cookies {:foo {:value "bar" :path "/"}}})]
    (is (= (cookies-get :foo) {:value "bar" :path "/"}))
    (is (= (cookies-get :bar) nil))))

(deftest test-cookies-set
  (binding [*response* (ref {})]
    (is (= (cookies-set :foo "bar") {:cookies {:foo {:value "bar" :path "/"}}}))
    (is (= (cookies-set :foo "bar" {:expires "Fri, 31-Dec-2010 23:59:59 GMT"})
           {:cookies {:foo {:value   "bar"
                            :path    "/"
                            :expires "Fri, 31-Dec-2010 23:59:59 GMT"}}}))
    (is (= @*response*
           {:cookies {:foo {:value   "bar"
                            :path    "/"
                            :expires "Fri, 31-Dec-2010 23:59:59 GMT"}}}))))
