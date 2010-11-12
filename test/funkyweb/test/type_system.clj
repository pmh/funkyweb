(ns funkyweb.test.type-system
  (:use [funkyweb.type-system]
        [lazytest.describe :only (describe it given do-it)]
        [lazytest.expect   :only (expect)]))

(describe coerce-to "tries to coerce value to type"
  (it "can coerce string to integer"
    (= (coerce-to [:int "1"]) 1))
  (it "can coerce integer to integer"
    (= (coerce-to [:int 1]) 1))
  (it "can coerce float to integer"
    (= (coerce-to [:int (Float. "1.3")]) 1))
  (it "can coerce double to integer"
    (= (coerce-to [:int (Double. "1.3")]) 1))
  (it "fails with an exception"
    (= (try (coerce-to [:int "abc"]) (catch Exception ex (.getMessage ex)))
       "Value must respond to to-int"))

  (it "can coerce string to float"
    (= (coerce-to [:float "1.3"]) (Float. "1.3")))
  (it "can coerce integer to float"
    (= (coerce-to [:float 1]) (Float. "1.0")))
  (it "can coerce double to float"
    (= (coerce-to [:float (Double. "1.3")]) (Float. "1.3")))
  (it "can coerce float to float"
    (= (coerce-to [:float (Float. "1.0")]) (Float. "1.0")))
  (it "fails with an exception"
    (= (try (coerce-to [:float "abc"]) (catch Exception ex (.getMessage ex)))
       "Value must respond to to-float"))

  (it "can coerce string to double"
    (= (coerce-to [:double "1.3"]) (Double. "1.3")))
  (it "can coerce integer to double"
    (= (coerce-to [:double 1]) (Double. "1.0")))
  (it "can coerce float to double"
    (= (coerce-to [:double (Float. "1.3")]) (Double. "1.3")))
  (it "can coerce double to double"
    (= (coerce-to [:double (Double. "1.3")]) (Double. "1.3")))
  (it "fails with an exception"
    (= (try (coerce-to [:double "abc"]) (catch Exception ex (.getMessage ex)))
       "Value must respond to to-double")))

(describe cast-hinted-args "with a type-hinted arglist and a list of values"
  (it "casts the hinted args"
    (= (cast-hinted-args [:int 'a :float 'b :double 'c] ["1" "2" "3"])
       [1 (Float. "2") (Double. "3")]))
  (it "won't cast non hinted args"
    (= (cast-hinted-args [:int 'a 'b 'c] ["1" "2" "3"])
       [1 "2" "3"])))

(describe args-list "with hinted-fn"
  (it "returns the functions arglist with any type hints removed"
    (= (args-list (hinted-fn [:int a b :float c]))
       ['a 'b 'c])))

(describe hinted-args-list "with hinted-fn"
  (it "returns the functions arglist with it's type-hints intact"
    (= (hinted-args-list (hinted-fn [:int "a" "b" :float "c"]))
       [:int "a" "b" :float "c"])))

(describe hinted-fn-meta "with a function and a hinted arglist"
  (it "returns a new fn with the hinted arglist attached to it's metadata"
    (= (:arglist (meta (hinted-fn-meta (fn [a] "") [:int 'a])))
       [:int 'a])))

(describe hinted-fn "with hinted arglist and body"
  (it "coerces any hinted args before execution"
    (= ((hinted-fn [:int a b :float c] [a b c]) "1" "foo" "2")
       [1 "foo" 2])))

(describe defhintedfn "with name hinted arglist and body"
  (it "defines name to point to a hinted-fn of arglist and body"
    (= (do (defhintedfn foo [:int a b :int c] [a b c])
           (foo "1" "foo" "2"))
       [1 "foo" 2])))
