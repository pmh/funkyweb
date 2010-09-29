(ns funkyweb.middleware.params
  "Parse form and query params. (modified version of the ring params middleware)"
  (:require [ring.util.codec :as codec]
            [clojure.string :as string]
            [clojure.contrib.io :as io]))

(defn- parse-params
  "Parse parameters from a string into a vector."
  [^String param-string encoding]
  (reduce
    (fn [param-vec encoded-param]
      (if-let [[_ _ val] (re-matches #"([^=]+)=(.*)" encoded-param)]
        (conj param-vec (codec/url-decode (or val "") encoding))
        param-vec))
    []
    (string/split param-string #"&")))

(defn- assoc-query-params
  "Parse and assoc parameters from the query string with the request."
  [request encoding]
  (merge-with merge request
    (if-let [query-string (:query-string request)]
      (let [params (parse-params query-string encoding)]
        {:query-params params, :params params}))))

(defn- urlencoded-form?
  "Does a request have a urlencoded form?"
  [request]
  (if-let [^String type (:content-type request)]
    (.startsWith type "application/x-www-form-urlencoded")))

(defn assoc-form-params
  "Parse and assoc parameters from the request body with the request."
  [request encoding]
  (merge-with merge request
    (if-let [body (and (urlencoded-form? request) (:body request))]
      (let [params (parse-params (slurp body) encoding)]
        {:params params}))))

(defn wrap-params
  "Middleware to parse urlencoded parameters from the query string and form
  body (if the request is a urlencoded form). Adds the following keys to
  the request map:
    :params       - a merged map of all types of parameter
  Takes an optional configuration map. Recognized keys are:
    :encoding - encoding to use for url-decoding. If not specified, uses
                the request character encoding, or \"UTF-8\" if no request
                character encoding is set."
  [handler & [opts]]
  (fn [request]
    (let [encoding (or (:encoding opts)
                       (:character-encoding request)
                       "UTF-8")
          request  (if (:form-params request)
                     request
                     (assoc-form-params request encoding))]
      (handler request))))
