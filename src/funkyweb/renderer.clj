(ns funkyweb.renderer
  (:use [funkyweb.response :only (safe-merge to-response content-type)]
        [funkyweb.router   :only (error-handlers)]
        [clojure.set       :only (select)]))

(defprotocol Renderer
  "Renders this as a valid Ring response"
  (render [this request response]))

(extend-protocol Renderer
  java.lang.String
  (render [this _ response] (safe-merge @response {:body this}))

  java.lang.Integer
  (render [this request response]
          (if-let [error-handler (first (select #(% this) @error-handlers))]
            (render ((get error-handler this) request response) request @response)
            (safe-merge @response {:status this})))
  
  clojure.lang.PersistentVector
  (render ([[status type body] _ response]
    (safe-merge @response (to-response status (content-type type) body))))

  clojure.lang.PersistentList
  (render [this _ response] (safe-merge @response {:body (str this)}))
  
  clojure.lang.PersistentHashMap
  (render [this _ response] (safe-merge @response this))

  clojure.lang.PersistentArrayMap
  (render [this _ response] (safe-merge @response this)))
