(ns funkyweb.test.middleware.multipart-params
  (:use clojure.test
        funkyweb.middleware.multipart-params
        [clojure.contrib.def :only (defvar-)])
  (:require [clojure.contrib.duck-streams :as du]
            [ring.util.test :as tu])
  (:import java.io.File))

(defvar- upload-content-type
  "multipart/form-data; boundary=----WebKitFormBoundaryAyGUY6aMxOI6UF5s")

(defvar- upload-content-length 188)

(defvar- upload-body (tu/string-input-stream
  "------WebKitFormBoundaryAyGUY6aMxOI6UF5s\r\nContent-Disposition: form-data; name=\"upload\"; filename=\"test.txt\"\r\nContent-Type: text/plain\r\n\r\nfoo\r\n\r\n------WebKitFormBoundaryAyGUY6aMxOI6UF5s--"))

(defvar- wrapped-echo (wrap-multipart-params identity))

(deftest test-wrap-multipart-params
  (let [req    {:content-type   upload-content-type
                :content-length upload-content-length
                :body           upload-body
                :params         ["bar"]}
        resp   (wrapped-echo req)
        params (flatten (:params resp))]
    (is (= "bar" (first params)))
    (let [upload (second params)]
      (is (= "test.txt" (:filename upload)))
      (is (= 5 (:size upload)))
      (is (= "text/plain" (:content-type upload)))
      (is (instance? File (:tempfile upload)))
      (is (= "foo\r\n" (du/slurp* (:tempfile upload)))))))
