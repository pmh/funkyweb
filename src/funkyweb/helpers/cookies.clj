(ns funkyweb.helpers.cookies
  (:require [funkyweb.helpers.response :as response]
            [funkyweb.helpers.request  :as request ]))

(defn cookies-get [k]
  (if-let [cookies (request/request-get :cookies)]
    (cookies k)))

(defn cookies-set
  ([k v] (cookies-set k v {}))
  ([k v options]
     (response/response-set :cookies {k (merge {:value v :path "/"} options)})))
