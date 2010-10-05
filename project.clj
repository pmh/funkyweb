(defproject funkyweb "0.1.0"
  :description "A ring based web framework for clojure"
  :dependencies     [[ org.clojure/clojure         "1.2.0"          ]
                     [ org.clojure/clojure-contrib "1.2.0"          ]
                     [ ring/ring-core              "0.2.5"          ]
                     [ ring/ring-jetty-adapter     "0.2.5"          ]
                     [ clout                       "0.2.0"          ]]
  :dev-dependencies [[ ring/ring-devel             "0.2.5"          ]
                     [ swank-clojure               "1.3.0-SNAPSHOT" ]
                     [ lein-clojars                "0.5.0-SNAPSHOT" ]])
