(ns funkyweb.test.controller.router
  (:use [funkyweb.controller.router] :reload-all)
  (:use [clojure.test]))

(deftest test-route-map
  (are [key] (not (nil? (key route-map)))
       :get :put :post :delete))

(deftest test-strip-type-hints
  (are [args-list expected]
       (= (strip-type-hints args-list))
       [:int 'a :int 'b :int 'c] ['a 'b 'c]
       ['a :int 'b :int 'c]      ['a 'b 'c]
       [:int 'a :int 'b 'c]      ['a 'b 'c]
       ['a 'b :int 'c]           ['a 'b 'c]
       ['a 'b 'c]                ['a 'b 'c]))

(deftest test-cast-hinted-args
  (are [args-list values expected]
       (= (cast-hinted-args args-list values) expected)
       [:int 'a :int 'b]    ["1" "2"]     [1 2]
       [:int 'a :int 'b 'c] ["1" "2" "3"] [1 2 "3"]
       ['a 'b 'c]           ["1" "2" "3"] ["1" "2" "3"]))

(deftest test-add-route
  (are [method name args action]
       (do
         (binding [controller-name (fn [] "dashboard")]
           (add-route method name args action)
           (= @(method route-map) {"/dashboard/foo/:bar/" {:action    action
                                                           :args-list args}})))
       :get    'foo ['bar]      "baz"
       :get    'foo [:int 'bar] "baz"
       :put    'foo ['bar]      "baz"
       :put    'foo [:int 'bar] "baz"
       :post   'foo ['bar]      "baz"
       :post   'foo [:int 'bar] "baz"
       :delete 'foo ['bar]      "baz"
       :delete 'foo [:int 'bar] "baz"))

(deftest test-add-error-handler
  (are [status-code body]
       (do
         (add-error-handler status-code body)
         (is (some #(= % [status-code body]) @error-map)))
       404 "404 - not found"
       500 "500 - internal server error"
       522 "The change you wanted was rejected."))

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
       'foo []          "/dashboard/foo/"
       'foo [:bar]      "/dashboard/foo/:bar/"
       'foo ['bar 'baz] "/dashboard/foo/:bar/:baz/"))

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

(deftest test-append-slash
  (are [s expected]
       (= (append-slash s) expected)
       "foo"      "foo/"
       "foo/"     "foo/"
       "foo/bar"  "foo/bar/"
       "foo/bar/" "foo/bar/"))

(deftest test-parse-args-list
  (are [args expected]
       (= (parse-args-list args) expected)
       ["foo" "bar"]          ["foo" "bar"]
       ["foo" "bar/baz"]      ["foo" "bar" "baz"]
       ["foo" "bar/baz/quux"] ["foo" "bar" "baz" "quux"]))

(deftest test-replace-varargs-with-star
  (= (replace-varargs-with-star ['& 'args])           ["*"])
  (= (replace-varargs-with-star ['foo '& 'args])      ['foo "*"])
  (= (replace-varargs-with-star ['foo 'bar '& 'args]) ['foo 'bar "*"]))

(deftest test-execute
  (are [req expected]
       (binding [deref (fn [_] {"/show/:id/" {:action    (fn [id] id)
                                             :args-list ['id]}})]
         (= (execute req) expected))
       {:request-method :get :uri "/show/10/"} "10"
       {:request-method :get :uri "/foo/"}     nil))

(deftest test-execute-raises
  (are [req expected]
       (binding [deref (fn [_] {"/show/:id/"
                               (fn [id] (throw (RuntimeException. "foo")))})]
         (= (try (execute req) (catch RuntimeException e "Exception raised"))
            expected))
       {:request-method :get :uri "/show/10"} "Exception raised"))
