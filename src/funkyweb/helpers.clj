(ns funkyweb.helpers
  (:use     [clojure.contrib.def :only (defalias)])
  (:require [funkyweb.helpers.session :as session]
            [funkyweb.helpers.cookies :as cookies]
            [funkyweb.helpers.request :as request]))

(defalias session-set   session/session-set)
(defalias session-get   session/session-get)

(defalias cookies-set   cookies/cookies-set)
(defalias cookies-get   cookies/cookies-get)

(defalias flash-get     flash/flash-get)
(defalias flash-set     flash/flash-set)

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
