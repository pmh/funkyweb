(ns funkyweb.test.controller
  (:use [funkyweb.controller]
        [funkyweb.router      :only (find-resource)]
        [lazytest.describe    :only (describe it given)]))


(describe handler "Looks up the resource associated with the request"
  (binding [find-resource (fn [routes req] (fn [] "foo"))]
    (given [req  {:uri "/"}
            resp (handler req)]
      (it "should render resource"
        (= {:status 200 :headers {"Content-Type" "text/html"} :uri "/" :body "foo"} resp)))))

