(ns funkyweb.helpers
  (:use [clojure.string      :only (split join)]
        [clojure.set         :only (select)]
        [ring.util.codec     :only (url-decode url-encode)]
        [funkyweb.router     :only (routes)]
        [funkyweb.utils      :only (str-interleave empty-seq?)]
        [funkyweb.response   :only (to-response response-merge!)]
        [funkyweb.controller :only (request)]))


(defn- qs-to-map [qs]
  (if-not (empty-seq? qs)
    (-> qs
        (.split "&")
        (->>
         (map #(vec (.split % "=")))
         (map #(vec [(keyword (first %))
                     (url-decode (second %))]))
         (into {})))
    {}))

(defn map-to-qs [m]
  (if-not (empty-seq? m)
    (-> m vec
        (->> (map (fn [[k v]] (str (name k) "=" (url-encode v))))
             (join "&")
             (str "?")))))

(defn- find-route [controller action]
  (first
   (select #(and (= (:controller %) controller)
                 (= (:action %) action)) @routes)))

(defn call [controller action & args]
  (apply (:resource (find-route controller action)) args))

(defn to-uri [route & args]
  (if args
    (let [qs   (if (map? (last args)) (last args))
          args (if (map? (last args)) (butlast args) args)
          base (-> (:path-spec route)
                   :regex
                   str
                   (split #"\(\[\^\/\.\,\;\?\]\+\)|\(\.\*\?\)"))]
      (str (apply str (interleave  args))
           (str-interleave "/" (-> (vec args) (subvec (count base))))
           (map-to-qs qs)))
    (-> (:path-spec route) :regex str)))

(defn redirect-to [controller action & args]
  (if-let [route (find-route controller action)]
    (let [path    (apply to-uri route args)
          status  303
          headers {"Location" path "Content-Type" "text/html"}
          body    (str "You're being redirected to: " path)]
      (to-response status headers body))))

(defn cookies-get [key]
  (let [key (str (name key))]
    (-> (:cookies request) (get key) :value)))

(defn cookies-set!
  ([key val] (cookies-set! key val {}))
  ([key val options]
     (response-merge! {:cookies {key (merge {:value val :path "/"} options)}})))

(defn session-get [key]
  (-> (:session request) key))

(defn session-set! [key val]
  (response-merge! {:session {key val}} {:session (:session request)}))

(defn flash-get [key]
  (-> (:flash request) key))

(defn flash-set! [key val]
  (response-merge! {:flash {key val}}))

(defn query-string
  ([]    (qs-to-map (:query-string request)))
  ([key] (key (qs-to-map (:query-string request)))))
