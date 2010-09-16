(ns funkyweb.server
  (:use funkyweb.controller.router
        funkyweb.response
        (ring.middleware stacktrace)))

(defn handler [req]
  (if-let [body (execute req)]
    (render [200 body])
    (render [404 (get @error-map 404)])))


(def app (-> #'handler
             (wrap-stacktrace)))

(defn server
  ([adapter-fn] (server adapter-fn {}))
  ([adapter-fn options]
     (adapter-fn app (merge {:port 8080 :join? false} options))))
