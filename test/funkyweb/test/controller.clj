(ns funkyweb.test.controller
  (:use [funkyweb.controller] :reload-all)
  (:use [clojure.test]))

(binding [*ns* (create-ns 'myapp.controllers.dashboard)]

  (defn execute [method uri]
    (funkyweb.controller.router/execute {:request-method method :uri uri}))


  (GET no-params [] "dashboard#no-params")
  
  (deftest test-no-params-builds-a-uri
    (= (no-params) "/dashboard/no-params"))
  (deftest test-no-params-action-method-returns-body
    (= (execute :get "/dashboard/no-params") "dashboard#no-params"))


  (GET say-hello [name] (str "Hello, " name "!"))

  (deftest test-say-hello-builds-a-uri
    (= (say-hello "foo") "/dashboard/say-hello/foo"))
  (deftest test-say-hello-action-method-returns-body
    (= (execute :get "/dashboard/say-hello/foo") "Hello, foo!"))


  (GET hello-foo []
       (str "<a href='" (say-hello "foo") "'>Say hello to foo" "</a>"))

  (deftest test-hello-foo-builds-a-uri
    (= (hello-foo) "/dashboard/hello-foo"))
  (deftest test-hello-foo-action-method-returns-body
    (= (execute :get "/dashboard/hello-foo")
       "<a href='/dashboard/say-hello/foo'>Say hello to foo</a>"))


  (GET add [:int a :int b] (str a " + " b " = " (+ a b)))

  (deftest test-add-builds-a-uri
    (= (add 1 2) "/dashboard/say-hello/1/2"))
  (deftest test-say-hello-action-method-returns-body
    (= (execute :get "/dashboard/add/1/2") "1 + 2 = 3"))


  (GET stuff [& stuff] (str stuff))

  (deftest test-stuff-builds-a-uri
    (= (stuff)             "/dashboard/stuff")
    (= (stuff "foo")       "/dashboard/stuff/foo")
    (= (stuff "foo" "bar") "/dashboard/stuff/foo/bar"))
  (deftest test-stuff-action-method-returns-body
    (= (execute :get "/dashboard/stuff")         "")
    (= (execute :get "/dashboard/stuff/foo")     "(\"foo\")")
    (= (execute :get "/dashboard/stuff/foo/bar") "(\"foo\" \"bar\")"))


  (error 404 "404 - Not found")

  (deftest test-invalid-route-returns-404-message
    (= (execute :get "/im/a/404") "404 - Not found")))
