(ns funkyweb.test.helpers.request
  (:use [funkyweb.helpers.request] :reload-all)
  (:use [clojure.test]))

(deftest test-request-get
  (binding [*request* (ref {:foo "bar"})]
    (is (= (request-get :foo) "bar"))))

(deftest test-query-string
  (binding [*request* (ref {:query-string "foo=bar&baz=quux"})]
    (is (= (query-string :foo) "bar"))
    (is (= (query-string :baz) "quux"))
    (is (= (query-string :w00t) nil))))

(deftest test-qs-to-map
  (is (= (qs-to-map "") {}))
  (is (= (qs-to-map "a=b") {:a "b"}))
  (is (= (qs-to-map "a=b&c=d") {:a "b" :c "d"}))
  (is (= (qs-to-map "a=b&c=d&e=f") {:a "b" :c "d" :e "f"}))
  (is (= (qs-to-map "foo=foo+bar") {:foo "foo bar"}))
  (is (= (qs-to-map "foo=foo%20bar") {:foo "foo bar"})))

(deftest test-map-to-qs
  (is (= (map-to-qs {}) ""))
  (is (= (map-to-qs {:a "b"}) "a=b"))
  (is (= (map-to-qs {:a "b" :c "d"}) "a=b&c=d"))
  (is (= (map-to-qs {:a "b" :c "d" :e "f"}) "a=b&c=d&e=f"))
  (is (= (map-to-qs {:foo "foo bar"}) "foo=foo+bar")))

(deftest test-resolve-content-type
  (is (= (resolve-content-type {:uri "/foo" :content-type nil})
         {:uri "/foo" :content-type "text/html"}))
  (is (= (resolve-content-type {:uri "/foo.html" :content-type nil})
         {:uri "/foo" :content-type "text/html"}))
  (is (= (resolve-content-type {:uri "/foo.xml" :content-type nil})
         {:uri "/foo" :content-type "text/xml"}))
  (is (= (resolve-content-type {:uri "/foo.json" :content-type nil})
         {:uri "/foo" :content-type "application/json"})))

(deftest test-restore-request-from
  (restore-request-from {:uri "/foo"})
  (is (= @*request* {:content-type "text/html" :uri "/foo"}))
  (restore-request-from {:uri "/foo.json"})
  (is (= @*request* {:content-type "application/json" :uri "/foo"}))
  (restore-request-from {:uri "/foo" :session {:foo "bar"}})
  (is (= @*request* {:content-type "text/html"
                     :uri          "/foo"
                     :session      {:foo "bar"}})))
