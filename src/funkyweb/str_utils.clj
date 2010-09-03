(ns funkyweb.str-utils)

(defn ends-with? [match s]
  (= (subs s (- (count s) (count match)))
     match))
