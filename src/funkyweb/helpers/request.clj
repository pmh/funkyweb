(ns funkyweb.helpers.request)

(declare alter-request)

(def *request* (ref {}))

(defn request-get [key]
  (@*request* key))

(defn restore-request-from [new-value]
  (dosync (ref-set *request* new-value)))
