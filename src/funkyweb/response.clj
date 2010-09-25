(ns funkyweb.response
  (:use funkyweb.controller.router
        (funkyweb.helpers session cookies response)))

(defn generate-response
  ([status]
     (generate-response status ""))
  ([status body]
     (generate-response status {"Content-Type" "text/html"} body))
  ([status headers body]
     (response merge {:status status :headers headers :body body})))

(defmulti render (fn [[_ x]] (type x)))

(defmethod render String [[status body]]
  (generate-response status body))

(defmethod render Integer [[_ error-code]]
  (let [resp (render [error-code (get @error-map error-code)])]
    (merge (generate-response error-code) resp)))

(defmethod render clojure.lang.PersistentVector [[_ v]]
  (generate-response (v 0) {"Content-Type" (v 1)} (v 2)))

(defmethod render clojure.lang.PersistentArrayMap [[status m]]
  (merge (generate-response status) m))
