(ns funkyweb.controller
  (:use funkyweb.controller.router)
  (:use (ring.middleware reload stacktrace)))


(defmacro GET [name args & forms]
  (do
    `(let [ns# *ns*]
       (add-route :get '~name '~args
                  (fn [~@(strip-type-hints args)]
                    ~@forms))
       (defn ~name [~@(strip-type-hints args)]
         (binding [*ns* ns#]
           (build-path '~name (strip-type-hints ~args)))))))

(defn generate-response [status body]
  {:status  status
   :headers {"Content-Type" "text/html"}
   :body    body})

(defn handler [req]
  (if-let [body (execute req)]
    (generate-response 200 body)
    (generate-response 404 "404 - not found")))


(def app (-> #'handler
             (wrap-stacktrace)))

(defn server [adapter-fn options]
  (adapter-fn (var app) options))
