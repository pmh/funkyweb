(ns funkyweb.test.router
  (:use [funkyweb.router]
        [funkyweb.type-system :only (hinted-fn)]
        [lazytest.describe    :only (describe it given with)]
        [lazytest.context     :only (fn-context)]))

(def regular-resource (hinted-fn [] "foo"))
(def id-resource      (hinted-fn [id] id))

(def regular-route {:path-spec "/foo"     :resource regular-resource})
(def id-route      {:path-spec "/foo/:id" :resource id-resource})

(def route-context (fn-context
                    (fn []
                      (binding [compile-route (constantly regular-route)]
                        (add-route regular-route)))
                    (fn [] (reset! routes []))))

(describe compile-route "compiles the route"
  (given [route          {:path-spec "/foo"}
          compiled-route (compile-route route)]
    (it "returns a new map with an updated :path-spec"
      (= (keys (:path-spec compiled-route))
         '(:absolute? :regex :keys)))))

(describe add-route "Conjoins the route onto routes"
  (with [route-context]
    (it "mutates the routes atom to include the route"
      (= @routes [{:path-spec "/foo" :resource regular-resource}]))))

(describe extract-args "extracts the arguments from the clout match object based on the resource's argument list"
  (given [no-args     (extract-args regular-route {})]
    (it "returns an empty argument list"
      (= no-args     [])))
  (given [with-id-arg (extract-args id-route {"id" "1"})]
    (it "returns a 1 element argument list"
      (= with-id-arg ["1"]))))

(describe route-matches "wraps clout's route-matches function"
  (given [req {:uri "/foo"}]
    (it "returns a vector the resource and an empty argument list"
      (= (route-matches regular-route req)
         [regular-resource []])))
  (given [req {:uri "/foo/10"}]
    (it "returns a vector of the resource and a 1 element argument list"
      (= (route-matches id-route req)
         [id-resource ["10"]]))))


(describe find-resource "finds a suitable resource for the request"
  (given [routes [id-route]
          req    {:uri "/foo/10"}]
    (it "returns a partial application of the resource with arguments applied"
      (= ((find-resource routes req)) "10"))))
