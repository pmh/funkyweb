(ns funkyweb.controller
  (:use [funkyweb.controller.parser       :only (parse-form)]
        [funkyweb.router                  :only (find-resource-for)]
        [funkyweb.utils                   :only (to-keyword)]
        [funkyweb.response                :only (response)]
        [funkyweb.renderer                :only (render)]
        [ring.middleware.params           :only (wrap-params)]
        [ring.middleware.multipart-params :only (wrap-multipart-params)]
        [ring.middleware.keyword-params   :only (wrap-keyword-params)]))

(declare request)

(defmacro defcontroller [name & forms]
  (doseq [form (map #(conj % (to-keyword name)) forms)]
    (parse-form form)))

(defn- with-corrected-req-method [req]
  (if-let [method (:_method req)]
    (assoc req :request-method (keyword (.toLowerCase method)))
    req))

(defn- handler [req]
  (binding [request  (with-corrected-req-method req)
            response (atom (merge response req))]
    (try (render ((find-resource-for req)) @response)
         (catch Exception ex (render [200 "text/html" "404 - not found"] {})))))
(def app
     (-> #'handler
         (wrap-params)
         (wrap-multipart-params)
         (wrap-keyword-params)))

(defn server [server-fn opts]
  (let [opts (merge {:port 8080 :join? false} opts)]
    (server-fn #'app opts)))
