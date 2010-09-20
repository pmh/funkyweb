(ns funkyweb.server
  (:use funkyweb.controller.router
        funkyweb.response
        (funkyweb.helpers session cookies)
        (ring.middleware session cookies)))

(defmacro wrap!
  "Lets you specify the middlewares you want to use.
    (wrap! (wrap-session cookie-store))
    => (def handler (wrap-session handler cookie-store))"
  [& middlewares]
  `(alter-var-root
    #'handler
    (constantly (-> handler ~@middlewares))))

(defn handler [req]
  (restore-session-from (:session req))
  (restore-cookies-from (:cookies req))
  (try
    (if-let [body (execute req)]
      (render [200 body])
      (render [404 (get @error-map 404)]))
    (catch java.lang.NumberFormatException e
        (render [404 (get @error-map 404)]))))

(defn server
  ([adapter-fn] (server adapter-fn {}))
  ([adapter-fn options]
     (do (wrap! (wrap-session)
                (wrap-cookies))
         (adapter-fn handler (merge {:port 8080 :join? false} options)))))
