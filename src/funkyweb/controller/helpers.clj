(ns funkyweb.controller.helpers
  (:use funkyweb.controller.router))

(defn controller-name-to-route [name]
  (apply str (replace {\- "/" \> ""} (str "/" name))))

(defmacro construct-url-helper [name args]
  `(let [ns# *ns*
         controller-name# *controller-name*]
     (defn ~name [~@(strip-type-hints args)]
       (binding [*ns* ns#
                 *controller-name* controller-name#]
         (let [& "&"]
           (build-path '~name
                       (flatten
                        (strip-type-hints (filter #(not (= "&" %)) ~args)))))))))

(defmacro construct-route [http-verb name args forms]
  `(add-route ~http-verb '~name (replace-varargs-with-star '~args)
              (fn [~@(strip-type-hints args)]
                ~@forms)))
