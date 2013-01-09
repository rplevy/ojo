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
                     "throttle"                   [throttle]
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
        (reset! result
                (format "%s%s"
                        (slurp file)
                        (if appended-only? "(append-only)" "")))
        (no-updates)))
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
