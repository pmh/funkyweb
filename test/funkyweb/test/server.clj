(ns funkyweb.test.server
  (:use [funkyweb.server] :reload-all)
  (:use [clojure.test]))

(deftest test-handler-with-existing-route
  (binding [funkyweb.controller.router/execute (fn [_] str "foo")]
    (is (= (handler {:request-method :get :uri "/dashboard/foobar"})
           {:cookies nil
            :status 200
            :headers {"Content-Type" "text/html"}
            :body "foo"}))))

(deftest test-handler-with-nonexisting-route
  (let [resp (handler {:request-method :get :uri "/dashboard/foobar" :body ""})]
    (is (= (resp :status) 404))
    (is (= (get (resp :headers) "Content-Type") "text/html"))
    (is (= (resp :body) "404 - not found"))))

(deftest test-server-with-adapter-fn
  (is (= (server (fn [_ options] options))
         {:port 8080 :join? false})))

(deftest test-server-with-adapter-fn-options
  (is (= (server (fn [_ options] options) {:port 9090 :join? true})
         {:port 9090 :join? true})))

(deftest test-handler-resets-response
  (binding [funkyweb.helpers.response/*response* (ref {:foo "bar"})]
    (handler {:request-method :get
              :uri "/dashboard/foo"
              :body ""})
    (is (= @funkyweb.helpers.response/*response* {}))))
