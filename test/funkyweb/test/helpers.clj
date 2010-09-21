(ns funkyweb.test.helpers
  (:use [funkyweb.helpers] :reload-all)
  (:use [clojure.test]))

(deftest test-respond-with
  (binding [funkyweb.helpers.request/*request*
            (ref {:content-type "text/html"})]
    (is (= (respond-with :html "html" :xml "xml" :json "json")
           [200 "text/html" "html"])))
  (binding [funkyweb.helpers.request/*request*
            (ref {:content-type "text/xml"})]
    (is (= (respond-with :html "html" :xml "xml" :json "json")
           [200 "text/xml" "xml"])))
  (binding [funkyweb.helpers.request/*request*
            (ref {:content-type "application/json"})]
    (is (= (respond-with :html "html" :xml "xml" :json "json")
           [200 "application/json" "json"])))
  (binding [funkyweb.helpers.request/*request*
            (ref {:content-type nil})]
    (is (= (respond-with :html "html" :xml "xml" :json "json")
           [200 "text/html" "html"]))))
