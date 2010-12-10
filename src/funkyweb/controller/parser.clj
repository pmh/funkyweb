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
  (let [[_ action arglist _] form
        controller (:controller (meta form))]
    (varargs-to-star (str-interleave "/" (name controller) action (map keyword (remove keyword? arglist))))))

(defn to-route [controller action method path-spec resource]
  {:controller     controller
   :action         action
   :request-method method
   :path-spec      path-spec
   :resource       resource})

(defn form-to-route [form]
  (let [controller (:controller (meta form))]
    (if (string? (nth form 2))
      (let [[method action uri arglist & body] form
            action (to-keyword action)
            method (to-keyword method)]
        (add-route
         (eval `(to-route ~controller ~action ~method ~uri (hinted-fn [~@arglist] ~@body)))))
      (let [[method action arglist & body] form
            method (to-keyword method)
            action (to-keyword action)
            path   (form-to-path form)]
        (add-route
         (eval `(to-route ~controller ~action ~method ~path (hinted-fn [~@arglist] ~@body))))))))

(defmulti parse-form first)
(defmethod parse-form 'GET [form]
  (form-to-route form))
(defmethod parse-form 'PUT [form]
  (form-to-route form))
(defmethod parse-form 'POST [form]
  (form-to-route form))
(defmethod parse-form 'DELETE [form]
  (form-to-route form))
