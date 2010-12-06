(ns funkyweb.router
  (:use [funkyweb.type-system :only (args-list hinted-args-list hinted-fn coerce-to)]
        [clojure.string       :only (split)]
        funkyweb.utils)
  (:require [clout.core :as clout]))

(defmethod coerce-to :map [[_ value]] value)

(def routes (atom []))

(defn compile-route [route]
  (let [path-spec (:path-spec route)]
    (assoc route :path-spec (clout/route-compile path-spec))))

(defn add-route [route]
  (swap! routes conj (compile-route route)))

(defn into-vec
  ([first] (vec first))
  ([first second] (into (vec first) second)))

(defn extract-uri-args [route match]
  (let [args-list (args-list (:resource route))
        varargs   (get match "*")]
    (remove nil? (into-vec (map #(get match (name %)) args-list)
                           (if varargs (split varargs #"\/"))))))

(defn extract-form-args
  ([route params]
     (let [arglist (hinted-args-list (:resource route))]
       (extract-form-args (filter #(or (= :map %) (symbol? %)) arglist) params [])))
  ([arglist params acc]
     (if-not (empty-seq? arglist)
       (let [arg  (first arglist)
             args (rest arglist)]
         (if (= :map arg)
           (recur (rest args) params (conj acc params))
           (recur args params (conj acc (get params (keyword arg))))))
       acc)))

(defn route-matches [route req]
  (if-let [match (clout/route-matches (:path-spec route) req)]
    (if (= :get (:request-method req))
      [(:resource route) (extract-uri-args  route match)]
      [(:resource route) (extract-form-args route (:params req))])))

(defn find-resource-for [req]
  (let [[resource args] (some (fn [route] (route-matches route req)) @routes)]
    (if (first args) (apply partial resource args) resource)))
