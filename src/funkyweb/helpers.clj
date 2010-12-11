(ns funkyweb.helpers
  (:use [clojure.string    :only (split)]
        [clojure.set       :only (select)]
        [funkyweb.router   :only (routes)]
        [funkyweb.utils    :only (str-interleave)]
        [funkyweb.response :only (to-response response-merge!)]))

(defn- find-route [controller action]
  (first
   (select #(and (= (:controller %) controller)
                 (= (:action %) action)) @routes)))

(defn- to-uri [route & args]
  (let [uri-regex #"\(\[\^\/\.\,\;\?\]\+\)|\(\.\*\?\)"
        path-spec (:regex (:path-spec route))
        uri-vec   (split (str path-spec) uri-regex)
        base-uri  (interleave uri-vec args)
        args      (str-interleave "/" (subvec (vec args) (count uri-vec)))]
    (apply str (conj (vec base-uri) args))))

(defn redirect-to [controller action & args]
  (if-let [route (find-route controller action)]
    (let [path    (apply to-uri route args)
          status  303
          headers {"Location" path "Content-Type" "text/html"}
          body    (str "You're being redirected to: " path)]
      (to-response status headers body))))

