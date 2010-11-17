(ns funkyweb.test.renderer
  (:use [funkyweb.renderer]
        [lazytest.describe :only (describe it given do-it)]
        [lazytest.expect   :only (expect)]))

(def fake-response {:status 200 :headers {"Content-Type" "text/html"}})

(describe Renderer
  (it "defines a signature for render"
    (= (get-in Renderer [:sigs :render :arglists])
       '([this response]))))

(describe java.lang.String "implements Renderer"
  (it "renders itself as a map"
    (= (render "foo" fake-response)
       {:status 200 :headers {"Content-Type" "text/html"} :body "foo"})))

(describe clojure.lang.PersistentVector "implements Renderer"
  (it "renders itself as a map"
    (= (render [404 "text/html" "404 - not found"] fake-response)
       {:status 404 :headers {"Content-Type" "text/html"} :body "404 - not found"})))

(describe clojure.lang.PersistentHashMap "implements Renderer"
  (it "renders itself as a map"
    (= (render (hash-map :status 500 :body "500 - internal server error") fake-response)
       {:status 500 :headers {"Content-Type" "text/html"} :body "500 - internal server error"})))

(describe clojure.lang.PersistentArrayMap "implements Renderer"
  (it "renders itself as a map"
    (= (render (array-map :status 500 :body "500 - internal server error") fake-response)
       {:status 500 :headers {"Content-Type" "text/html"} :body "500 - internal server error"})))
