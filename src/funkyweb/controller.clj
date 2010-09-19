(ns funkyweb.controller
  (:use funkyweb.controller.router
        funkyweb.controller.helpers))

(defn controller-name-to-route [name]
  (apply str (replace {\- "/" \> ""} (str "/" name))))

(defmacro defcontroller [controller-name & forms]
  `(do (binding [*controller-name* (controller-name-to-route '~controller-name)]
         ~@forms)
       nil))

(defmacro GET [name args & forms]
  `(do
     (construct-url-helper ~name ~args)
     (construct-route :get ~name ~args ~forms)))

(defn error [status-code body]
  (add-error-handler status-code body))
