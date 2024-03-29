(ns funkyweb.helpers
  (:use     [clojure.contrib.def :only (defalias)])
  (:require [funkyweb.helpers.request  :as request]
            [funkyweb.helpers.response :as response]
            [funkyweb.helpers.session  :as session]
            [funkyweb.helpers.cookies  :as cookies]
            [funkyweb.helpers.flash    :as flash]
            [ring.util.response        :as ring-response]
            [ring.util.codec           :as ring-codec]))

(defalias request-get   request/request-get)
(defalias query-string  request/query-string)
(defalias qs            request/query-string)

(defalias response-set  response/response-set)

(defalias session-set   session/session-set)
(defalias session-get   session/session-get)

(defalias cookies-set   cookies/cookies-set)
(defalias cookies-get   cookies/cookies-get)

(defalias flash-get     flash/flash-get)
(defalias flash-set     flash/flash-set)

(defalias file-response     ring-response/file-response)
(defalias resource-response ring-response/resource-response)
(defalias url-encode        ring-codec/url-encode)
(defalias url-decode        ring-codec/url-decode)
(defalias base64-encode     ring-codec/base64-encode)
(defalias base64-decode     ring-codec/base64-decode)

(defn redirect-to [f & args]
  (let [uri (if (string? f) f (apply f args))]
    (response/response merge
                       {:status  301
                        :headers {"Location" uri}
                        :body    (str "You are being redirected to: " uri)})))

(defn respond-with [& {:keys [html xml json]}]
  (condp = (request-get :content-type)
      "text/html"        [200 "text/html"        html]
      "text/xml"         [200 "text/xml"         xml ]
      "application/json" [200 "application/json" json]
      [200 "text/html" html]))
