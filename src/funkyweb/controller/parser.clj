(ns funkyweb.controller.parser
  (:use [funkyweb.router       :only (add-route add-error-handler!)]
        [funkyweb.type-system  :only (hinted-fn)]
        [funkyweb.utils        :only (str-interleave to-keyword)]
        [clojure.string        :only (split)]))

(defn- varargs-to-star [path]
  (let [path (apply str path)]
   (if (re-find #"\:&|\:map" path)
     (str (first (split path #"\:&|\:map")) "*")
     path)))

(defn form-to-path [form]
  (let [[method action arglist _] form
        controller (name (:controller (meta form)))
        arglist    (map keyword (remove keyword? arglist))]
    (if (= method 'GET)
      (varargs-to-star (str-interleave "/" controller action arglist))
      (str-interleave "/" controller action))))

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
            method (to-keyword method)
            arglist (if (not= :get method) (replace {'& :map} arglist) arglist)]
        (add-route
         (eval `(to-route ~controller ~action ~method ~uri (hinted-fn [~@arglist] ~@body)))))
      (let [[method action arglist & body] form
            method (to-keyword method)
            action (to-keyword action)
            path   (form-to-path form)
            arglist (if (not= :get method) (replace {'& :map} arglist) arglist)]
        (add-route
         (eval `(to-route ~controller ~action ~method ~path (hinted-fn [~@arglist] ~@body))))))))

(defn to-fn [arglist body]
  (eval (conj '() body arglist 'fn)))

(defn parse-body [body]
  (if (string? (first (flatten body)))
    (first (flatten body))
    body))

(defmulti parse-form first)

(defmethod parse-form 'GET [form]
  (form-to-route form))

(defmethod parse-form 'PUT [form]
  (form-to-route form))

(defmethod parse-form 'POST [form]
  (form-to-route form))

(defmethod parse-form 'DELETE [form]
  (form-to-route form))

(defmethod parse-form 'error [form]
  (let [[_ type & body] form
        body (to-fn '[request response] (conj body 'do))]
    (if (symbol? type)
      (add-error-handler! {(symbol (.getName (eval type))) body})
      (add-error-handler! {type body}))))
