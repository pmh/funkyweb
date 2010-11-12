(ns funkyweb.type-system)

(defprotocol TypeCoercer
  (to-int    [this])
  (to-float  [this])
  (to-double [this]))

(extend-protocol TypeCoercer
  java.lang.Integer
  (to-int    [this] this)
  (to-float  [this] (Float/parseFloat   (str this)))
  (to-double [this] (Double/parseDouble (str this)))
  
  java.lang.Double
  (to-int    [this] (Math/round this))
  (to-float  [this] (Float/parseFloat   (str this)))
  (to-double [this] this)

  java.lang.Float
  (to-int    [this] (Math/round this))
  (to-float  [this] this)
  (to-double [this] (Double/parseDouble (str this)))

  java.lang.String
  (to-int    [this] (to-int (to-float this)))
  (to-float  [this] (Float/parseFloat   this))
  (to-double [this] (Double/parseDouble this)))

(defmacro try-or-throw
  "Wraps expr in a try/catch clause. If an exception is
  caught it throws the provided exception expr"
  [expr exception]
  `(try ~expr (catch Exception ex# (throw ~exception))))

(defmulti coerce-to first)

(defmethod coerce-to :int [[_ value]]
  (try-or-throw (to-int value) (Exception. "Value must respond to to-int")))

(defmethod coerce-to :float  [[_ value]]
  (try-or-throw (to-float value) (Exception. "Value must respond to to-float")))

(defmethod coerce-to :double [[_ value]]
  (try-or-throw (to-double value) (Exception. "Value must respond to to-double")))


(defn cast-hinted-args [hinted-args-list args-list]
  (loop [acc [] hinted-args hinted-args-list args args-list]
    (if (seq args)
      (if (keyword? (first hinted-args))
        (recur (conj acc (coerce-to [(first hinted-args) (first args)]))
               (rest (rest hinted-args))
               (rest args))
        (recur (conj acc (first args))
               (rest hinted-args)
               (rest args)))
      acc)))

(defn args-list [f]
  (filter symbol? (:arglist (meta f))))

(defn hinted-args-list [f]
  (:arglist (meta f)))

(defn hinted-fn-meta [f hinted-args-list]
  (with-meta f {:arglist hinted-args-list}))

(defmacro hinted-fn [args & body]
  `(hinted-fn-meta
    (fn [& args#]
      (apply (fn [~@(filter symbol? args)] ~@(do body))
             (cast-hinted-args '~args args#)))
    '~args))

(defmacro defhintedfn
  "Let's you define functions with type hinted arguments.
  By type hinting arguments you promise to call the function
  either with the specified type or something which can be
  converted to that type.

  (defhintedfn foo [:int a :int b]
    (+ a b))

  (foo 1 1)     ;=> 2
  (foo \"1\" \"1\") ;=> 2
  (foo \"a\" \"b\") ;=> Value must respond to to-int [Thrown class java.lang.Exception]
  "
  [name args & body]
  `(def ~name (hinted-fn ~args ~@body)))
