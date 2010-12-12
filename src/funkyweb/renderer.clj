(ns funkyweb.renderer
  (:use [funkyweb.response :only (safe-merge to-response content-type)]
        [funkyweb.router   :only (error-handlers)]
        [clojure.set       :only (select)]))

(defprotocol Renderer
  "Renders this as a valid Ring response"
  (render [this response]))

(extend-protocol Renderer
  java.lang.String
  (render [this response] (safe-merge response {:body this}))

  java.lang.Integer
  (render [this response]
          (if-let [error-handler (first (select #(% this) @error-handlers))]
            (render (eval (error-handler this)) response)
            (safe-merge response {:status this})))
  
  clojure.lang.PersistentVector
  (render ([[status type body] response]
    (safe-merge response (to-response status (content-type type) body))))

  clojure.lang.PersistentHashMap
  (render [this response] (safe-merge response this))

  clojure.lang.PersistentArrayMap
  (render [this response] (safe-merge response this)))
