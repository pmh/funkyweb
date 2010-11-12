(ns funkyweb.controller
  (:use funkyweb.type-system
        funkyweb.router))

(defn render-to-response [resource]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body    (resource)})

(defn handler [req]
  (render-to-response (find-resource @routes req)))
