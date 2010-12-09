(ns funkyweb.controller.parser
  (:use [funkyweb.router       :only (add-route)]
        [funkyweb.type-system  :only (hinted-fn)]
        [funkyweb.utils        :only (str-interleave to-keyword)]
        [clojure.string        :only (split)]))

(defn- varargs-to-star [path]
  (let [path (apply str path)]
   (if (re-find #"\:&|\:map" path)
     (str (first (split path #"\:&|\:map")) "*")
     path)))

(defn form-to-path [form]
  (let [[controller _ action arglist _] form]
    (varargs-to-star (str-interleave "/" (name controller) action (map keyword (remove keyword? arglist))))))

(defn to-route [controller action method path-spec resource]
  {:controller     controller
   :action         action
   :request-method method
   :path-spec      path-spec
   :resource       resource})

(defn form-to-route [form]
  (if (string? (nth form 3))
    (let [[controller method action uri arglist & body] form]
      (add-route
       (eval `(to-route ~controller ~(to-keyword action) ~(to-keyword method) ~uri (hinted-fn [~@arglist] ~@body)))))
    (let [[controller method action arglist & body] form]
      (add-route
       (eval `(to-route ~controller ~(to-keyword action) ~(to-keyword method) ~(form-to-path form) (hinted-fn [~@arglist] ~@body)))))))

(defmulti parse-form second)
(defmethod parse-form 'GET [form]
  (form-to-route form))
(defmethod parse-form 'PUT [form]
  (form-to-route form))
(defmethod parse-form 'POST [form]
  (form-to-route form))
(defmethod parse-form 'DELETE [form]
  (form-to-route form))
