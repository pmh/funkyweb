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

(defn- with-corrected-req-method [req]
  (if-let [method (:_method req)]
    (assoc req :request-method (keyword (.toLowerCase method)))
    req))

(defn- handler [req]
  (binding [request  (with-corrected-req-method req)
            response (atom (merge response req))]
    (try
      (render ((find-resource-for req)) @response)
      (catch Exception ex
        (let [exception (symbol (first (split (str ex) #":")))]
          (if-let [error-handler (first (select exception @error-handlers))]
            (render (eval (get error-handler exception)) @response)
            (render 404 @response)))))))

(def ^{:private true} app
  (-> #'handler
      (wrap-params)
      (wrap-multipart-params)
      (wrap-keyword-params)))

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
