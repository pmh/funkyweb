(ns funkyweb.controller
  (:use [funkyweb.controller.impl :only (build-route *controller-name* request)]
        [funkyweb.router          :only (add-route find-resource-for)]
        [funkyweb.response        :only (response)]
        [funkyweb.renderer        :only (render)]))

(defmacro defcontroller [name & forms]
  `(binding [*controller-name* (str '~name)]
     ~@forms))

(defmacro GET [name uri? arglist & body]
  `(add-route ~(build-route (list :get name uri? arglist body))))

(defmacro PUT [name uri? arglist & body]
  `(add-route ~(build-route (list :put name uri? arglist body))))

(defmacro POST [name uri? arglist & body]
  `(add-route ~(build-route (list :post name uri? arglist body))))

(defmacro DELETE [name uri? arglist & body]
  `(add-route ~(build-route (list :delete name uri? arglist body))))

(defn- handler [req]
  (binding [request  req
            response (atom (merge response req))]
    (try (render ((find-resource-for req)) @response)
         (catch Exception ex (render [200 "text/html" "404 - not found"] {})))))
