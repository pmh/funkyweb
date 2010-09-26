(ns funkyweb.test.response
  (:use [funkyweb.response] :reload-all)
  (:use [clojure.test]))

(deftest test-generate-response-with-status
  (is (= (generate-response 200)
         {:cookies nil
          :status  200
          :headers {"Content-Type" "text/html"}
          :body    ""})))

(deftest test-generate-response-with-status-body
  (is (= (generate-response 200 "foo")
         {:cookies nil
          :status 200
          :headers {"Content-Type" "text/html"}
          :body    "foo"})))

(deftest test-generate-response-with-status-headers-body
  (is (= (generate-response 200 {"Content-Type" "text/xml"} "foobar")
         {:cookies nil
          :status 200
          :headers {"Content-Type" "text/xml"}
          :body    "foobar"})))

(deftest test-generate-response-merges-cookies
  (binding [funkyweb.helpers.cookies/*cookies* (ref {:foo "bar"})]
    (is (= (generate-response 200 {"Content-Type" "text/xml"} "foobar")
           {:cookies {:foo "bar"}
            :status 200
            :headers {"Content-Type" "text/xml"}
            :body    "foobar"}))))


(deftest test-render-with-string
  (is (= (render [200 "with string"])
         {:cookies nil
          :status  200
          :headers {"Content-Type" "text/html"}
          :body    "with string"})))

(deftest test-render-with-integer
  (is (= (render [404 404])
         {:cookies nil
          :status  404
          :headers {"Content-Type" "text/html"}
          :body    "404 - not found"})))

(deftest test-render-with-vector
  (is (= (render [200 [200 "text/xml" "with vector"]])
         {:cookies nil
          :status  200
          :headers {"Content-Type" "text/xml"}
          :body    "with vector"})))

(deftest test-render-with-map
  (is (= (render [200 {:status 200
                       :headers {"Content-Type" "text/html"}
                       :body "with map"}])
         {:cookies nil
          :status  200
          :headers {"Content-Type" "text/html"}
          :body    "with map"})))
