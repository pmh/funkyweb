(ns funkyweb.server
  (:use funkyweb.controller.router
        funkyweb.response
        (ring.middleware session cookies)))

(defmacro wrap!
  "Lets you specify the middlewares you want to use.
    (wrap! (wrap-session cookie-store))
    => (def handler (wrap-session foo cookie-store))"
  [& middlewares]
  `(alter-var-root
    #'handler
    (constantly (-> handler ~@middlewares))))

(defn handler [req]
  (try
    (if-let [body (execute req)]
      (render [200 body])
      (render [404 (get @error-map 404)]))
    (catch java.lang.NumberFormatException e
        (render [404 (get @error-map 404)]))))

(defn server
  ([adapter-fn] (server adapter-fn {}))
  ([adapter-fn options]
     (adapter-fn app (merge {:port 8080 :join? false} options))))
