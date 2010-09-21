(ns funkyweb.helpers.request
  (:use ring.util.codec))

(declare alter-request qs-to-map)

(def *request* (ref {}))

(def content-types {"json" "application/json"
                    "xml"  "text/xml"
                    "html" "text/html"})

(defn request-get [key]
  (@*request* key))

(defn query-string [key]
  (try
    (key (qs-to-map (request-get :query-string)))
    (catch NullPointerException e
      nil)))

(defn qs-to-map [qs]
  (-> qs
      (.split "&")
      (->>
       (map #(vec (.split % "=")))
       (map #(vec [(keyword (first %))
                   (url-decode (second %))]))
       (into {}))))

(defn resolve-content-type [req]
  (let [uri-vec      (-> (req :uri) (clojure.string/split #"\."))
        content-type (content-types (last uri-vec))]
    (if content-type
      (merge req {:content-type content-type
                  :uri (apply str (butlast uri-vec))})
      req)))

(defn restore-request-from [req]
  (dosync (ref-set *request* (resolve-content-type req))))
