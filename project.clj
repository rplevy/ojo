(defproject ojo "1.1.0"
  :description "Respond to file system events using the Java 7 Watch Service API."
  :url "https://clojars.org/ojo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/core.incubator "0.1.0"]
                 [useful "0.8.2"]
                 [commons-io/commons-io "2.3"]
                 [fs "1.2.0"]]
  :profiles {:dev {:dependencies [[midje "1.4.0"]]}}
  :plugins [[lein-midje "2.0.0-SNAPSHOT"]])
