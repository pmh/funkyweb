(ns funkyweb.test.controller
  (:use [funkyweb controller server] :reload-all)
  (:use [clojure.test]))

(defcontroller dashboard

  (defn execute
    ([method uri] (execute method uri ""))
    ([method uri body]
       (binding [slurp identity]
         (funkyweb.controller.router/execute {:request-method method
                                              :uri uri
                                              :body body}))))

  (GET no-params [] "dashboard#no-params")

  (GET say-hello [name] (str "Hello, " name "!"))

  (GET hello-foo []
    (str "<a href='" (say-hello "foo") "'>Say hello to foo" "</a>"))  

  (GET add [:int a :int b] (str a " + " b " = " (+ a b)))

  (GET stuff [& stuff] (str stuff))

  (POST no-params-post []
    "dashboard#no-params-post")

  (POST with-params-post [param]
    param)

  (PUT no-params-put []
    "dashboard#no-params-put")

  (PUT with-params-put [param]
    param)
  
  (DELETE no-params-delete []
    "dashboard#no-params-delete")

  (DELETE with-params-delete [param]
    param)
  
  (error 404 "404 - Not found")
  
  (let [route-map funkyweb.controller.router/route-map
        error-map funkyweb.controller.router/error-map]

    (deftest test-no-params-builds-a-uri
      (is (= (no-params) "/dashboard/no-params"))
      (is (= (no-params {:foo "bar"}) "/dashboard/no-params?foo=bar")))
    (deftest test-no-params-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :get "/dashboard/no-params")
               "dashboard#no-params"))))
    
    (deftest test-say-hello-builds-a-uri
      (is (= (say-hello "foo") "/dashboard/say-hello/foo"))
      (is (= (say-hello "foo" {:foo "bar"}) "/dashboard/say-hello/foo?foo=bar")))
    (deftest test-say-hello-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :get "/dashboard/say-hello/foo") "Hello, foo!"))))
    
    (deftest test-hello-foo-builds-a-uri
      (is (= (hello-foo) "/dashboard/hello-foo"))
      (is (= (hello-foo {:foo "bar"}) "/dashboard/hello-foo?foo=bar")))
    (deftest test-hello-foo-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :get "/dashboard/hello-foo")
               "<a href='/dashboard/say-hello/foo'>Say hello to foo</a>"))))
    
    (deftest test-add-builds-a-uri
      (is (= (add 1 2) "/dashboard/add/1/2"))
      (is (= (add 1 2 {:foo "bar"}) "/dashboard/add/1/2?foo=bar")))
    (deftest test-add-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :get "/dashboard/add/1/2") "1 + 2 = 3"))))
    (deftest test-add-action-method-returns-404-for-non-numeric-routes
      (binding [funkyweb.controller.router/error-map error-map]
        (is (= (handler {:request-method :get
                         :uri "/dashboard/add/1/foo"
                         :body ""})
               {:status 404
                :headers {"Content-Type" "text/html"}
                :body "404 - Not found"}))))
    
    (deftest test-stuff-builds-a-uri
      (is (= (stuff)                     "/dashboard/stuff"))
      (is (= (stuff "foo")               "/dashboard/stuff/foo"))
      (is (= (stuff "foo" {:foo "bar"})  "/dashboard/stuff/foo?foo=bar"))
      (is (= (stuff "foo" "bar")         "/dashboard/stuff/foo/bar"))
      (is (= (stuff "foo" "bar" {:foo "bar"})
             "/dashboard/stuff/foo/bar?foo=bar")))
    (deftest test-stuff-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :get "/dashboard/stuff/")        ""))
        (is (= (execute :get "/dashboard/stuff/foo")     "(\"foo\")"))
        (is (= (execute :get "/dashboard/stuff/foo/bar") "(\"foo\" \"bar\")"))))
    
    (deftest test-invalid-route-returns-404-message
      (binding [funkyweb.controller.router/error-map error-map]
        (is (= (handler {:request-method :get :uri "/im/a/404" :body ""})
               {:status 404,
                :headers {"Content-Type" "text/html"},
                :body "404 - Not found"}))))

    (deftest test-post-url-helper
      (is (= (no-params-post) "/dashboard/no-params-post"))
      (is (= (with-params-post) "/dashboard/with-params-post"))
      (is (= (with-params-post "foo") "/dashboard/with-params-post/foo")))
    (deftest test-post-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :post "/dashboard/no-params-post/")
               "dashboard#no-params-post"))
        (is (= (execute :post "/dashboard/with-params-post/foo") "foo"))))

    (deftest test-put-url-helper
      (is (= (no-params-put) "/dashboard/no-params-put"))
      (is (= (with-params-put) "/dashboard/with-params-put"))
      (is (= (with-params-put "foo") "/dashboard/with-params-put/foo")))
    (deftest test-put-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :put "/dashboard/no-params-put/")
               "dashboard#no-params-put"))
        (is (= (execute :put "/dashboard/with-params-put/foo") "foo"))))

    (deftest test-delete-url-helper
      (is (= (no-params-delete) "/dashboard/no-params-delete"))
      (is (= (with-params-delete) "/dashboard/with-params-delete"))
      (is (= (with-params-delete "foo") "/dashboard/with-params-delete/foo")))
    (deftest test-delete-action-method-returns-body
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :delete "/dashboard/no-params-delete/")
               "dashboard#no-params-delete"))
        (is (= (execute :delete "/dashboard/with-params-delete/foo") "foo"))))))

(defcontroller blog->:id->posts

  (GET show [id post_id]
    (str "blog_id: " id " post_id: " post_id))

    
  (let [route-map funkyweb.controller.router/route-map
        error-map funkyweb.controller.router/error-map]

    (deftest test-nested-controller-works
      (binding [funkyweb.controller.router/route-map route-map]
        (is (= (execute :get "/blog/10/posts/show/20")
               (str "blog_id: " 10 " post_id: " 20)))))))
