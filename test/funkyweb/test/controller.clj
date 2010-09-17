(ns funkyweb.test.controller
  (:use [funkyweb controller server] :reload-all)
  (:use [clojure.test]))

(defcontroller dashboard

  (defn execute [method uri]
    (funkyweb.controller.router/execute {:request-method method :uri uri}))

  (GET no-params [] "dashboard#no-params")

  (GET say-hello [name] (str "Hello, " name "!"))

  (GET hello-foo []
    (str "<a href='" (say-hello "foo") "'>Say hello to foo" "</a>"))  

  (GET add [:int a :int b] (str a " + " b " = " (+ a b)))

  (GET stuff [& stuff] (str stuff))

  (error 404 "404 - Not found")
  
  (let [route-map funkyweb.controller.router/route-map
        error-map funkyweb.controller.router/error-map]

    (deftest test-no-params-builds-a-uri
      (is (= (no-params) "/dashboard/no-params")))
    (deftest test-no-params-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :get "/dashboard/no-params")
               "dashboard#no-params"))))
    
    (deftest test-say-hello-builds-a-uri
      (is (= (say-hello "foo") "/dashboard/say-hello/foo")))
    (deftest test-say-hello-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :get "/dashboard/say-hello/foo") "Hello, foo!"))))
    
    (deftest test-hello-foo-builds-a-uri
      (is (= (hello-foo) "/dashboard/hello-foo")))
    (deftest test-hello-foo-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :get "/dashboard/hello-foo")
               "<a href='/dashboard/say-hello/foo'>Say hello to foo</a>"))))
    
    (deftest test-add-builds-a-uri
      (is (= (add 1 2) "/dashboard/add/1/2")))
    (deftest test-add-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :get "/dashboard/add/1/2") "1 + 2 = 3"))))
    (deftest test-add-action-method-returns-404-for-non-numeric-routes
      (binding [funkyweb.controller.router/error-map error-map]
        (is (= (handler {:request-method :get
                         :uri "/dashboard/add/1/foo"})
               {:status 404,
                :headers {"Content-Type" "text/html"},
                :body "404 - Not found"}))))
    
    (deftest test-stuff-builds-a-uri
      (is (= (stuff)             "/dashboard/stuff/"))
      (is (= (stuff "foo")       "/dashboard/stuff/foo"))
      (is (= (stuff "foo" "bar") "/dashboard/stuff/foo/bar")))
    (deftest test-stuff-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :get "/dashboard/stuff/")         ""))
        (is (= (execute :get "/dashboard/stuff/foo")     "(\"foo\")"))
        (is (= (execute :get "/dashboard/stuff/foo/bar") "(\"foo\" \"bar\")"))))
    
    (deftest test-invalid-route-returns-404-message
      (binding [funkyweb.controller.router/error-map error-map]
        (is (= (handler {:request-method :get :uri "/im/a/404"})
               {:status 404,
                :headers {"Content-Type" "text/html"},
                :body "404 - Not found"}))))))
