(ns funkyweb.controller
  (:use [funkyweb.controller.parser       :only (parse-form)]
        [funkyweb.router                  :only (find-resource-for error-handlers)]
        [funkyweb.utils                   :only (to-keyword)]
        [funkyweb.response                :only (response)]
        [funkyweb.renderer                :only (render)]
        [ring.middleware.params           :only (wrap-params)]
        [ring.middleware.multipart-params :only (wrap-multipart-params)]
        [ring.middleware.keyword-params   :only (wrap-keyword-params)]
        [clojure.set                      :only (select)]
        [clojure.string                   :only (split)]))

(declare request)

(defn- with-controller-meta [controller form]
  (with-meta form {:controller (to-keyword controller)}))

(defmacro defcontroller [name & forms]
  (doseq [form (map with-controller-meta (repeat name) forms)]
    (parse-form form)))

(defn- fix-request-method [req]
  (if-let [method (:_method req)]
    (assoc req :request-method (keyword (.toLowerCase method)))
    req))

(defn handler [req]
  (binding [request  (fix-request-method req)
            response (atom response)]
    (try
      (render ((find-resource-for req)) request @response)
      (catch Exception ex
        (let [exception (symbol (.getName (.getClass ex)))]
          (if-let [error-handler (first (select exception @error-handlers))]
            (render ((eval (get error-handler exception)) request response) request @response)
            (render 404 request @response)))))))

(def ^{:private true} app
  (-> #'handler
      (wrap-keyword-params)
      (wrap-params)
      (wrap-multipart-params)))

(defmacro wrap!
  [& middlewares]
  `(alter-var-root
    #'handler
    (constantly (-> handler ~@middlewares))))

(defn server
  ([server-fn] (server server-fn {}))
  ([server-fn opts]
      (let [opts (merge {:port 8080 :join? false} opts)]
        (server-fn #'app opts))))
