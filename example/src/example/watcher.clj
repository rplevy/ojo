(ns example.watcher
  (:require [milieu.config :as config]
            [ojo.watch :refer :all]
            [ojo.extension
             [track-appends :refer [track-appends]]
             [throttle :refer [throttle]]]
            [clojure.java.io :as io]))

(def result (atom nil))

(defn start [parallel? dir extensions-str]
  (let [extensions (condp = extensions-str
                     "throttle" [throttle]
                     "throttle and track-appends" [throttle track-appends]
                     [])]
    (defwatch watcher
      [dir [["*goodstring.dat" #"^\S+$"]]] [:create :modify]
      {:parallel parallel?
       :worker-poll-ms (when parallel? (config/value :watcher :worker-poll-ms))
       :worker-count (when parallel? (config/value| :watcher :worker-count))
       :extensions extensions
       :settings {:throttle-period (config/value :watcher :throttle-period)}}
      (let [[{:keys [file kind appended-only? bit-position] :as evt}]
            *events*]
        (swap! result
               (fn [r]
                 (str (when r (str r ","))
                      (if appended-only?
                        (str "\n" (slurp file))
                        (with-open [rdr (io/reader file)]
                          (slurp (doto rdr (.skip (or bit-position 0)))))))))))
    (start-watch watcher)))

(defn -main [& [env & args]]
  (assert (config/env? (keyword env))
          (format "\n\n\"%s\" is not a valid environment\n" env))
  (config/commandline-overrides! args)
  (config/with-env env
    (println "starting watcher...")
    (start
     ;; command-line usage:
     ;; lein run -m example.watcher dev --watcher.parallel true --watcher.dir '"/tmp/"' --watcher.extensions '"throttle and track-appends"'
     (config/value :watcher :parallel)
     (config/value :watcher :dir)
     (config/value :watcher :extensions))))
