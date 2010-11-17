(ns funkyweb.renderer
  (:use [funkyweb.response :only (safe-merge to-response content-type)]))

(defprotocol Renderer
  "Renders this as a valid Ring response"
  (render [this response]))

(extend-protocol Renderer
  java.lang.String
  (render [this response] (safe-merge response {:body this}))

  clojure.lang.PersistentVector
  (render ([[status type body] response]
    (safe-merge response (to-response status (content-type type) body))))

  clojure.lang.PersistentHashMap
  (render [this response] (safe-merge response this))

  clojure.lang.PersistentArrayMap
  (render [this response] (safe-merge response this)))
