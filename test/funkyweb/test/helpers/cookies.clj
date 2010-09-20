(ns funkyweb.test.helpers.cookies
  (:use [funkyweb.helpers.cookies] :reload-all)
  (:use [clojure.test]))

(deftest test-cookies-get
  (binding [*cookies* (ref {})]
    (cookies-set :foo "bar")
    (is (= (cookies-get :foo) {:value "bar" :path "/"}))))

(deftest test-cookies-set-without-options
  (binding [*cookies* (ref {})]
    (cookies-set :foo "bar")
    (is (= @*cookies* {:foo {:value "bar" :path "/"}}))))

(deftest test-cookies-set-with-options
  (binding [*cookies* (ref {})]
    (cookies-set :foo "bar" {:expires "Fri, 31-Dec-2010 23:59:59 GMT"})
    (is (= (@*cookies* :foo) {:value   "bar"
                              :path    "/"
                              :expires "Fri, 31-Dec-2010 23:59:59 GMT"}))
    (cookies-set :bar "baz" {:path "/foo"})
    (is (= (@*cookies* :bar) {:value "baz"
                              :path  "/foo"}))))

(deftest test-alter-cookies
  (binding [*cookies* (ref {})]
    (is (= (alter-cookies assoc :foo "bar" :baz "quux")
           {:foo "bar" :baz "quux"}))
    (is (= (alter-cookies dissoc :baz)
           {:foo "bar"}))))
