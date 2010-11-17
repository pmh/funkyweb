(ns funkyweb.response)

(def response {:status 200 :headers {"Content-Type" "text/html"}})

(defn merge-if [pred]
  (fn [& vals] (if (every? pred vals) (apply merge vals) (last vals))))

(defn safe-merge [& maps]
  (apply merge-with (merge-if map?) maps))

(defn response-merge! [& maps]
  (apply swap! response safe-merge @response maps))

(defn to-response [status headers body]
  {:status status :headers headers :body body})

(defn content-type [type]
  {"Content-Type" type})
