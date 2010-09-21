(ns funkyweb.controller.helpers
  (:use funkyweb.controller.router)
  (:require [clojure.string :as string]))

(defn controller-name-to-route [name]
  (string/replace (str "/" name) #"->" "/"))

(defmacro construct-url-helper [name args]
  `(let [controller-name# *controller-name*]
     (def ~name
          (fn [~@(strip-type-hints args)]
            (binding [*controller-name* controller-name#]
              (let [& "&"]
                (build-path '~name
                            (flatten
                             (strip-type-hints (filter #(not (= "&" %)) ~args))))))))))

(defmacro construct-route [http-verb name args forms]
  `(add-route ~http-verb '~name (replace-varargs-with-star '~args)
              (fn [~@(strip-type-hints args)]
                ~@forms)))
