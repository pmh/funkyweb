(ns funkyweb.test.controller.router
  (:use [funkyweb.controller.router] :reload-all)
  (:use [clojure.test]))

(deftest test-route-map
  (are [key] (= @(key route-map) {})
       :get :put :post :delete))


(deftest test-add-route
  (are [method route action]
       (do
         (add-route method route action)
         (= @(method route-map) {route action}))
       :get    "foo" "bar"
       :put    "foo" "bar"
       :post   "foo" "bar"
       :delete "foo" "bar"))

(deftest test-ns-name-to-str
  (binding [*ns* (create-ns 'funkyweb.test.controller.router)]
    (is (= "funkyweb.test.controller.router" (ns-name-to-str)))))

(deftest test-controller-name
  (are [ns-name expected]
       (binding [ns-name-to-str #(str ns-name)]
         (= (controller-name) expected))
       "some-ns.controllers.dashboard" "dashboard"
       "some-ns.controllers.blog.post" "blog/post"))

(deftest test-build-route
  (are [name args expected]
       (binding [ns-name-to-str #(str "some-ns.controllers.dashboard")]
         (= (build-route name args) expected))
       'foo []          "/dashboard/foo"
       'foo [:bar]      "/dashboard/foo/:bar"
       'foo ['bar 'baz] "/dashboard/foo/:bar/:baz"))

(deftest test-build-path
  (are [name args expected]
       (binding [ns-name-to-str #(str "some-ns.controllers.dashboard")]
         (= (build-path name args) expected))
       'bar []      "/dashboard/bar"
       'foo [10]    "/dashboard/foo/10"
       'foo [10 20] "/dashboard/foo/10/20"))

(deftest test-match-route
  (are [uri route expected]
       (= (match-route uri route) expected)
       "/show/10"    "/show/:id" ["/show/:id" (seq ["10"])]
       "/show/10/10" "/show/:id" nil
       "/show/10"    "/foo/:id"  nil))

(deftest test-execute
  (are [req expected]
       (binding [deref (fn [_] {"/show/:id" (fn [id] id)})]
         (= (execute req) expected))
       {:request-method :get :uri "/show/10"} "10"
       {:request-method :get :uri "/foo"}     nil))

(deftest test-execute-raises
  (are [req expected]
       (binding [deref (fn [_] {"/show/:id"
                               (fn [id] (throw (RuntimeException. "foo")))})]
         (= (try (execute req) (catch RuntimeException e "Exception raised"))
            expected))
       {:request-method :get :uri "/show/10"} "Exception raised"))
