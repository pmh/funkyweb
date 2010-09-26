(ns funkyweb.helpers.session
  (:require [funkyweb.helpers.response :as response]
            [funkyweb.helpers.request  :as request ]))

(defn session-get [k]
  (if-let [session (request/request-get :session)]
    (session k)))

(defn session-set [k v & kvs]
  (let [kvs-map (into {} (map vec (partition 2 kvs)))]
    (response/response-set :session (assoc kvs-map k v))))
