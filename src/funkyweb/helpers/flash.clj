(ns funkyweb.helpers.flash
  (:require [funkyweb.helpers.response :as response]
            [funkyweb.helpers.request  :as request ]))

(defn flash-set [k v & kvs]
  (let [kvs-map (into {} (map vec (partition 2 kvs)))]
    (response/response-set :flash (assoc kvs-map k v))))

(defn flash-get [k]
  (if-let [flash (request/request-get :flash)]
    (flash k)))
