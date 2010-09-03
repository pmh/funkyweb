(ns funkyweb.test.str-utils
  (:use [funkyweb.str-utils] :reload-all)
  (:use [clojure.test]))

(deftest test-ends-with?
  (are [match s expected]
       (= (ends-with? match s) expected)
       "/" "foo/" true
       "/" "foo"  false))
