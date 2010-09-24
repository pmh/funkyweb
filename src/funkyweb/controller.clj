(ns funkyweb.controller
  (:use funkyweb.controller.router
        funkyweb.controller.helpers))

(defmacro defcontroller [controller-name & forms]
  `(do (binding [*controller-name* (controller-name-to-route '~controller-name)]
         ~@forms)
       nil))

(defmacro GET [name args & forms]
  `(do
     (construct-url-helper ~name ~args)
     (construct-route :get ~name ~args ~forms)))

(defmacro POST [name args & forms]
  `(do
     (construct-url-helper ~name ~args)
     (construct-route :post ~name ~args ~forms)))

(defmacro PUT [name args & forms]
  `(do
     (construct-url-helper ~name ~args)
     (construct-route :put ~name ~args ~forms)))

(defmacro DELETE [name args & forms]
  `(do
     (construct-url-helper ~name ~args)
     (construct-route :delete ~name ~args ~forms)))

(defn error [status-code body]
  (add-error-handler status-code body))
