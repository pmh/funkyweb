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

(defn extract-args [route match]
  (let [args-list (args-list (:resource route))
        varargs   (get match "*")]
    (remove nil? (into (vec (map #(get match (name %)) args-list))
                       (if varargs (split varargs #"\/"))))))

(defn route-matches [route req]
  (if-let [match (clout/route-matches (:path-spec route) req)]
    [(:resource route) (extract-args route match)]))

(defn find-resource [routes req]
  (let [[resource args] (some (fn [route] (route-matches route req)) routes)]
    (if (first args) (apply partial resource args) resource)))
