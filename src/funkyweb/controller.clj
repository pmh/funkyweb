(ns funkyweb.controller
  (:use funkyweb.type-system
        funkyweb.router))

(declare request)

(defn render-to-response [body]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body    body})

(defn handler [req]
  (render-to-response (binding [request req] ((find-resource @routes req)))))
