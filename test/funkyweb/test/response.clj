(ns funkyweb.test.response
  (:use [funkyweb.response]
        [lazytest.describe :only (describe it given do-it)]
        [lazytest.expect   :only (expect)]))

(describe response
  (it "defines a default response"
    (= response {:status 200 :headers {"Content-Type" "text/html"}})))

(describe safe-merge
  (it "only merges if value is a map"
    (= (safe-merge
        {:foo "the foo" :baz {:foo "the foo" :bar "the bar" :baz "the baz"}}
        {:foo "a foo"   :baz {:baz "a baz"}})
       {:foo "a foo" :baz {:foo "the foo" :bar "the bar" :baz "a baz"}})))

(describe to-response
  (it "turns its arguments into a ring response"
    (= (to-response 200 {"Content-Type" "text/html"} "foo")
       {:status 200 :headers {"Content-Type" "text/html"} :body "foo"})))

(describe content-type
  (it "returns a header map woth provided content-type"
    (= (content-type "text/html")
       {"Content-Type" "text/html"})))
