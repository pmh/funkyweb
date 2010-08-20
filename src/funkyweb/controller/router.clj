(ns funkyweb.controller.router
  (:use [clojure.contrib.str-utils])
  (:use [clout.core]))

(def route-map {:get  (atom {}) :put    (atom {})
                :post (atom {}) :delete (atom {})})

(defn add-route
  "Looks up method in the route-map, associates
  route with action and adds them to the map it
  references"
  [method route action]
  (swap! (method route-map) assoc route action))

(defn ns-name-to-str
  "Grabs the ns-name of *ns* and calls str on it"
  []
  (str (ns-name *ns*)))

(defn controller-name
  "Grabs the name of the current namespace and
  extracts everything after 'controllers',
  replacing . with / in the process.

  (binding [*ns* (create-ns 'myapp.controllers.foo)]
    (controller-name)) ;=> 'foo'
  
  (binding [*ns* (create-ns 'myapp.controllers.foo.bar)]
    (controller-name)) ;=> 'foo/bar'"
  []
  (re-gsub #"\." "/"
           (last
            (re-find #".*\.controllers\.(.*)" (ns-name-to-str)))))

(defn- build-uri [name args]
  (let [base (str "/" (controller-name) "/" name)]
    (if (seq args)
      (str base "/" (str-join "/" args))
      base)))

(defn build-route
  "Builds a route from the name and args
  
  (binding [*ns* (create-ns 'myapp.controllers.foo)]
    (build-route 'show [])) ;=> /foo/show
  
  (binding [*ns* (create-ns 'myapp.controllers.foo)]
    (build-route 'show '[bar baz])) ;=> /foo/show/:bar/:baz"
  [name args]
  (build-uri name (map keyword args)))

(defn build-path
  "Builds a path from the name and args
  
  (binding [*ns* (create-ns 'myapp.controllers.foo)]
    (build-route 'show [])) ;=> /foo/show
  
  (binding [*ns* (create-ns 'myapp.controllers.foo)]
    (build-route 'show [1 2])) ;=> /foo/show/1/2"
  [name args]
  (build-uri name args))

(defn match-route
  "Finds the route, if any, which matches the uri.
  Returns the route and the matched keywords.

  (match-route \"/product/10\" \"/product/:id\")
    ;=> [\"/product/:id\" (10)]

  (match-route \"/product/10/20\" \"/product/:id/:limit\")
    ;=> [\"/product/:id/:limit\" (10 20)]"
  [uri route]
  (if-let [match (route-matches route uri)]
    [route (vals match)]))

(defn execute
  "Extracts the request-method and uri from the request
  and tries to find a route matching that signature.
  Returns either the result of executing the action
  if one is found or nil otherwise"
  [req]
  (let [method       (:request-method req)
        uri          (:uri req)
        [route args] (some (partial match-route uri) (keys @(method route-map)))]
    (if-let [action (get @(method route-map) route)]
      (apply action args))))
