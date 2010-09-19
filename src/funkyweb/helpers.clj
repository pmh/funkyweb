(ns funkyweb.helpers
  (:use     [clojure.contrib.def :only (defalias)])
  (:require [funkyweb.helpers.session :as session]))

(defalias session-set   session/session-set)
(defalias session-get   session/session-get)
(defalias alter-session session/alter-session)
