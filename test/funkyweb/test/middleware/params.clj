(ns funkyweb.test.middleware.params
  (:use clojure.test
        funkyweb.middleware.params)
  (:require [ring.util.test :as tu]))

(def wrapped-echo (wrap-params identity))

(deftest wrap-params-form-params
  (let [req  {:content-type "application/x-www-form-urlencoded"
              :body         (tu/string-input-stream "biz=bat%25")}
        resp (wrapped-echo req)]
    (is (= ["bat%"] (:params resp)))))

(deftest wrap-params-strips-method-and-sets-it-as-req-method
  (let [req {:content-type "application/x-www-form-urlencoded"
             :body         (tu/string-input-stream "_method=put&biz=bat")}
        resp (wrapped-echo req)]
    (is (= ["bat"] (:params resp)))
    (is (= :put (:request-method resp)))))

(deftest wrap-params-not-form-encoded
  (let [req  {:content-type "application/json"
              :body         (tu/string-input-stream "{foo: \"bar\"}")}
        resp (wrapped-echo req)]
    (is (empty? (:params resp)))))

(deftest wrap-params-always-conj-vectors
  (let [req  {:query-string ""
              :content-type "application/x-www-form-urlencoded"
              :body         (tu/string-input-stream "")}
        resp (wrapped-echo req)]
    (is (= [] (:params resp)))))
