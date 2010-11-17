(ns funkyweb.controller
  (:use [funkyweb type-system router response renderer]))

(declare request)

(defn handler [req]
  (binding [request  req
            response (atom (merge response req))]
    (render ((find-resource @routes req)) @response)))
