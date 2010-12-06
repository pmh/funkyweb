(ns funkyweb.controller.impl
  (:use [funkyweb type-system router response renderer utils])
  (:use [clojure.string :only (blank?)]))

(declare request)

(def *controller-name* "")

(def http-verbs #{'get 'post 'put 'delete})

(def reserved-words #{:& :map})

(def non-reserved-words
     (complement (fn [elem] (reserved-words (first elem)))))

(defmacro webfn [arglist body]
  (if `(some #(and (keyword? %) (not (= :map %))) '~arglist)
    `(hinted-fn [~@arglist] ~@body)
    `(with-meta (fn ['~@arglist] ~@body) {:arglist ['~@arglist]})))

(defn varargs-to-star [arglist]
  (if (empty-seq? (filter #{:& :map} arglist))
    arglist
    (let [filtered-list (filter non-reserved-words
                                (partition-by reserved-words arglist))]
      (conj (vec (first filtered-list)) "*"))))

(defn build-path [form]
  `(let [[method# name# arglist# & body#] '~form
         keyword-args#    (map keyword arglist#)
         controller-name# (.toLowerCase *controller-name*)]
     (if (= :get method#)
       (str-interleave "/" controller-name# name# (varargs-to-star keyword-args#))
       (str-interleave "/" controller-name# name#))))

(defn to-route-map [method path resource]
  {:request-method method :path-spec path :resource resource})

(defn build-route [form]
  (if (string? (nth form 2))
    `(let [[method# name# uri# arglist# body#] '~form]
       (to-route-map method# uri# (webfn [~@(nth form 3)] ~(nth form 4))))
    `(let [[method# name# arglist# & body#] '~form]
       (to-route-map method# ~(build-path form) (webfn [~@(nth form 2)] ~(nth form 3))))))
