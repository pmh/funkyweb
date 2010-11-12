(ns funkyweb.test.controller
  (:use [funkyweb.controller]
        [funkyweb.router      :only (find-resource)]
        [lazytest.describe    :only (describe it given)]))

(def root-route      {:path-spec "/"          :resource  (fn []        "/ was called")})
(def route-with-args {:path-spec "/:id/:name" :resource  (fn [id name] (str "id " id " name " name))})

(describe handler "Looks up the resource accociated with the uri of the request"
  (binding [find-resource (fn [routes req] (:resource root-route))]
    (given [req  {:uri "/"}
            resp (handler req)]
      (it "should find the resource and render a ring response"
        (= {:status 200 :headers {"Content-Type" "text/html"} :body "/ was called"} resp))))

  (binding [find-resource (fn [routes req] (partial (:resource route-with-args) 10 "pmh"))]
    (given [req  {:uri "/10/pmh"}
            resp (handler req)]
      (it "should find the resource apply args and render a ring response"
        (= {:status 200 :headers {"Content-Type" "text/html"} :body "id 10 name pmh"} resp)))))
