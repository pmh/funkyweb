(ns funkyweb.test.controller.router
  (:use [funkyweb.controller.router] :reload-all)
  (:use [clojure.test]))

(deftest test-route-map
  (are [key] (not (nil? (key route-map)))
       :get :put :post :delete))

(deftest test-strip-type-hints-ints
  (are [args-list expected]
       (= (strip-type-hints args-list))
       [:int 'a :int 'b :int 'c] ['a 'b 'c]
       ['a :int 'b :int 'c]      ['a 'b 'c]
       [:int 'a :int 'b 'c]      ['a 'b 'c]
       ['a 'b :int 'c]           ['a 'b 'c]
       ['a 'b 'c]                ['a 'b 'c]))

(deftest test-strip-type-hints-floats
  (are [args-list expected]
       (= (strip-type-hints args-list))
       [:float 'a :float 'b :float 'c] ['a 'b 'c]
       ['a :float 'b :float 'c]        ['a 'b 'c]
       [:float 'a :float 'b 'c]        ['a 'b 'c]
       ['a 'b :float 'c]               ['a 'b 'c]
       ['a 'b 'c]                      ['a 'b 'c]))

(deftest test-strip-type-hints-doubles
  (are [args-list expected]
       (= (strip-type-hints args-list))
       [:double 'a :double 'b :double 'c] ['a 'b 'c]
       ['a :double 'b :double 'c]         ['a 'b 'c]
       [:int 'a :double 'b 'c]            ['a 'b 'c]
       ['a 'b :double 'c]                 ['a 'b 'c]
       ['a 'b 'c]                         ['a 'b 'c]))

(deftest test-strip-type-hints-multiple
  (are [args-list expected]
       (= (strip-type-hints args-list))
       [:int 'a :float 'b :double 'c] ['a 'b 'c]
       ['a :int 'b :double 'c]        ['a 'b 'c]
       [:double 'a :float 'b 'c]      ['a 'b 'c]))

(deftest test-cast-hinted-args-ints
  (are [args-list values expected]
       (= (cast-hinted-args args-list values) expected)
       [:int 'a :int 'b]    ["1" "2"]     [1 2]
       [:int 'a :int 'b 'c] ["1" "2" "3"] [1 2 "3"]
       ['a 'b 'c]           ["1" "2" "3"] ["1" "2" "3"]))

(deftest test-cast-hinted-args-floats
  (are [args-list values expected]
       (= (cast-hinted-args args-list values) expected)
       [:float 'a :float 'b]    ["5.01" "0.32"] [(Float/parseFloat "5.01")
                                                 (Float/parseFloat "0.32")]
       [:float 'a :float 'b 'c] ["1" "1.2" "4.03"] [(Float/parseFloat "1")
                                                    (Float/parseFloat "1.2")
                                                    "4.03"]
       ['a 'b 'c]               ["1" "2" "3"]      ["1" "2" "3"]))

(deftest test-cast-hinted-args-doubles
  (are [args-list values expected]
       (= (cast-hinted-args args-list values) expected)
       [:double 'a :double 'b]    ["5.01" "0.32"]    [(Double/parseDouble "5.01")
                                                      (Double/parseDouble "0.32")]
       [:double 'a :double 'b 'c] ["1" "1.2" "4.03"] [(Double/parseDouble "1")
                                                      (Double/parseDouble "1.2")
                                                      "4.03"]
       ['a 'b 'c]                 ["1" "2" "3"]      ["1" "2" "3"]))

(deftest test-add-error-handler
  (are [status-code body]
       (do
         (add-error-handler status-code body)
         (is (some #(= % [status-code body]) @error-map)))
       404 "404 - not found"
       500 "500 - internal server error"
       522 "The change you wanted was rejected."))

(deftest test-build-route
  (are [name args expected]
       (binding [*controller-name* "/dashboard"]
         (= (build-route name args) expected))
       'foo []          "/dashboard/foo/"
       'foo [:bar]      "/dashboard/foo/:bar/"
       'foo ['bar 'baz] "/dashboard/foo/:bar/:baz/"))

(deftest test-build-path
  (are [name args expected]
       (binding [*controller-name* "/dashboard"]
         (= (build-path name args) expected))
       'bar []                   "/dashboard/bar"
       'foo [10]                 "/dashboard/foo/10"
       'foo [10 20]              "/dashboard/foo/10/20"
       'foo [{:foo "bar"}]       "/dashboard/foo?foo=bar"
       'foo [10 {:foo "bar"}]    "/dashboard/foo/10?foo=bar"
       'foo [10 20 {:foo "bar"}] "/dashboard/foo/10/20?foo=bar"))

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
       {:request-method :get :uri "/show/10/" :body ""} "10"
       {:request-method :get :uri "/foo/"     :body ""} nil))

(deftest test-execute-raises
  (are [req expected]
       (binding [deref (fn [_] {"/show/:id/"
                               (fn [id] (throw (RuntimeException. "foo")))})]
         (= (try (execute req) (catch RuntimeException e "Exception raised"))
            expected))
       {:request-method :get :uri "/show/10"} "Exception raised"))
