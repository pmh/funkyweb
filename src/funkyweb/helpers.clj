(ns funkyweb.helpers
  (:use     [clojure.contrib.def :only (defalias)])
  (:require [funkyweb.helpers.session :as session]
            [funkyweb.helpers.cookies :as cookies]
            [funkyweb.helpers.request :as request]))

(defalias session-set   session/session-set)
(defalias session-get   session/session-get)
(defalias alter-session session/alter-session)

(defalias cookies-set   cookies/cookies-set)
(defalias cookies-get   cookies/cookies-get)
(defalias alter-cookies cookies/alter-cookies)

(defalias request-get   request/request-get)
(defalias query-string  request/query-string)
(defalias qs            request/query-string)

