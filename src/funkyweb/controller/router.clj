(ns funkyweb.controller.router
  (:use [clojure.contrib.str-utils]
        [funkyweb.str-utils]
        [clout.core]))

(def *controller-name* nil)

(def route-map {:get  (atom {}) :put    (atom {})
                :post (atom {}) :delete (atom {})})

(def error-map (atom {}))

(defn strip-type-hints
  "Takes an argument list and removes any
  type-hints it may contain.
  
  (strip-type-hints [:int 'a :float 'b :double 'c])
    ;=> ['a 'b 'c]

  (strip-type-hints ['a 'b])
    ;=> ['a 'b]"
  [args]
  (filter (complement #{:int :double :float}) args))

(defn cast-hinted-args
  "Takes the original argument list and the
  real values parsed from the url and replaces
  type-hints with type-casts

  (cast-hinted-args [:int 'a] [\"1\"])
      ;=> [(Integer/parseInt \"1\")]

  (cast-hinted-args [:float 'a] [\"1.2\"])
      ;=> [(Float/parseFloat \"1.2\")]

  (cast-hinted-args [:double 'a] [\"4.6\"])
      ;=> [(Double/parseDouble \"4.6\")]"
  [args-list values]
  (loop [args args-list, vals values, ret []]
    (if (seq args)
      (condp = (first args)
          :int    (recur (rest (rest args)) (rest vals)
                         (conj ret (Integer/parseInt (first vals))))
          :float  (recur (rest (rest args)) (rest vals)
                         (conj ret (Float/parseFloat (first vals))))
          :double (recur (rest (rest args)) (rest vals)
                         (conj ret (Double/parseDouble (first vals))))
          (recur (rest args) (rest vals) (conj ret (first vals))))
      ret)))

(defn- build-uri [name args]
  (let [base (str "/" *controller-name* "/" name)]
    (if (seq args)
      (str base "/" (str-join "/" args))
      (str base))))

(defn build-route
  "Builds a route from the name and args
  
  (binding [*controller-name* \"foo\"]
    (build-route 'show [])) ;=> /foo/show
  
  (binding [*controller-name* \"foo\"]
    (build-route 'show '[bar baz])) ;=> /foo/show/:bar/:baz"
  [name args]
  (let [keyworded-args (into [] (map keyword (filter #(not (= "*" %)) args)))]
    (build-uri name (conj keyworded-args (first (filter #(= "*" %) args))))))

(defn build-path
  "Builds a path from the name and args
  
  (binding [*controller-name* \"foo\"]
    (build-path 'show [])) ;=> /foo/show
  
  (binding [*controller-name* \"foo\"]
    (build-path 'show [1 2])) ;=> /foo/show/1/2"
  [name args]
  (build-uri name args))

(defn add-route
  "Looks up method in the route-map, associates
  route with action and adds them to the map it
  references"
  [method name args action]
  (do
    (swap! (method route-map) assoc (build-route name (strip-type-hints args))
           {:action action :args-list args})
    nil))

(defn add-error-handler [status-code body]
  (do (swap! error-map assoc status-code body)))

(defn match-route
  "Finds the route, if any, which matches the uri.
  Returns the route and the matched keywords.

  (match-route \"/product/10\" \"/product/:id\")
    ;=> [\"/product/:id\" (10)]

  (match-route \"/product/10/20\" \"/product/:id/:limit\")
    ;=> [\"/product/:id/:limit\" (10 20)]
  
  (match-route \"/product/10.26\" \"/product/:price\")
    ;=> [\"/product/:id/:limit\" (10.26 20)]"
  [uri route]
  (let [escaped-uri (apply str (replace {\. \-} uri))]
    (if-let [match (route-matches route escaped-uri)]
      [route (map #(apply str (replace {\- \.} %)) (vals match))])))

(defn append-slash
  "Appends a forward slash to the end of the string
  unless one is already present

  (append-slash \"foo\")
    ;=> \"foo/\"

  (append-slash \"foo/\")
    ;=> \"foo/\""
  [s]
  (if (ends-with? "/" s)
    s
    (str s "/")))

(defn parse-args-list
  "Variadic arguments are passed in as a string with the
  arguments separated by slashes (/). This functions turns
  that string into a sequence and conjoins it with the other
  arguments.

  (parse-args-list [\"a\" \"b\" \"c/d/e\"])
    ;=> (\"a\" \"b\" \"c\" \"d\" \"e\")

  (parse-args-list [\"a\" \"b\"])
    ;=> (\"a\" \"b\")"
  [args]
  (let [var-args (re-split #"\/" (last (flatten args)))]
    (into (apply vector (butlast args))
          (if-not (empty? (first var-args)) var-args []))))

(defn replace-varargs-with-star [args]
  "Searches parameter list for a variadic declaration
  and replaces it with a star (*) if found

  (replace-varargs-with-star ['foo & args])
    ;=> ['foo \"*\"]

  (replace-varargs-with-star ['foo & stuff]
    ;=> ['foo \"*\"]"
  (if (some #(= '& %) args)
    (loop [a args ret []]
      (if (seq a)
        (if (= '& (first a))
          (recur [] (conj ret "*"))
          (recur (rest a) (conj ret (first a))))
        ret))
    args))

(defn execute
  "Extracts the request-method and uri from the request
  and tries to find a route matching that signature.
  Returns either the result of executing the action
  if one is found or nil otherwise"
  [req]
  (let [method       (:request-method req)
        uri          (append-slash (:uri req))
        [route args] (some (partial match-route uri) (keys @(method route-map)))]
    (if-let [match (get @(method route-map) route)]
      (let [type-casted-args (cast-hinted-args (:args-list match) (reverse args))]
        (apply (:action match)
               (if (= "*" (last (:args-list match)))
                 (parse-args-list type-casted-args)
                 type-casted-args))))))
