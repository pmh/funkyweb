(ns funkyweb.controller
  (:use funkyweb.controller.router)
  (:use (ring.middleware stacktrace)))

(defmacro defcontroller [controller-name & forms]
  `(do (binding [*controller-name* (str '~controller-name)]
         ~@forms)
       nil))

(defmacro construct-url-helper [name args]
  `(let [ns# *ns*
         controller-name# *controller-name*]
     (defn ~name [~@(strip-type-hints args)]
       (binding [*ns* ns#
                 *controller-name* controller-name#]
         (let [& "&"]
           (build-path '~name
                       (flatten
                        (strip-type-hints (filter #(not (= "&" %)) ~args)))))))))

(defmacro construct-route [http-verb name args forms]
  `(add-route ~http-verb '~name (replace-varargs-with-star '~args)
              (fn [~@(strip-type-hints args)]
                ~@forms)))

(defmacro GET [name args & forms]
  `(do
     (construct-url-helper ~name ~args)
     (construct-route :get ~name ~args ~forms)))

(defn error [status-code body]
  (add-error-handler status-code body))

(defn generate-response [status body]
  {:status  status
   :headers {"Content-Type" "text/html"}
   :body    body})

(defn handler [req]
  (if-let [body (execute req)]
    (generate-response 200 body)
    (generate-response 404 (get @error-map 404))))


(def app (-> #'handler
             (wrap-stacktrace)))

(defn server
  ([adapter-fn] (server adapter-fn {}))
  ([adapter-fn options]
     (adapter-fn app (merge {:port 8080 :join? false} options))))
