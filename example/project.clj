(defproject example "-"
  :description "Demonstrate uses of ojo."
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ojo "1.0.0"]
                 [ojo.extension "1.0.0"]
                 [milieu "0.7.0"]
                 [fs "1.2.0"]]
  :profiles {:dev {:dependencies [[midje "1.5-alpha6"]]}}
  :plugins [[lein-cucumber "1.0.1"]])
