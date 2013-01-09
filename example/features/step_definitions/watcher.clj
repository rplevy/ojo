(require '[midje.sweet :refer :all]
         '[ojo.watch :refer [cease-watch]]
         '[milieu.config :as config]
         '[useful.utils :refer [defm]]
         '[fs.core :as fs]
         '[darker-watcher.csi.core :as watcher])

(Given #"the watcher is not watching" []
  (when (:cease watcher/dat-file-watcher)
    (cease-watch watcher/dat-file-watcher)
    (Thread/sleep 7000)))

(Given #"the watcher is watching \"([^\"]*)\"$" [directory]
  (when (:cease watcher/dat-file-watcher)
    (cease-watch watcher/dat-file-watcher)
    (Thread/sleep 7000))
  (future
    (config/commandline-overrides! ["--watcher.workers" "10"
                                    "--watcher.worker-poll-ms" "1000"])
    (config/with-env :test (watcher/main* directory nil)))
  (Thread/sleep 7000))
