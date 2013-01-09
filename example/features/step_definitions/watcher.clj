(require '[midje.sweet :refer :all]
         '[ojo.watch :refer [cease-watch]]
         '[milieu.config :as config]
         '[useful.utils :refer [defm]]
         '[fs.core :as fs]
         '[example.watcher :as watcher])

(Given #"^the result atom is empty" []
  (reset! watcher/result nil))

(Given #"^the result atom should be empty" []
  (assert (fact @watcher/result => nil)))

(Then #"the following data should be in the result atom" [s]
  (assert (fact @watcher/result => s)))

(Given #"a watcher enabling extension \"([^\"]*)\" is watching \"([^\"]*)\" (serially|using parallel workers)" [extensions-str directory parallel-str]
  (when (:cease watcher/watcher)
    (cease-watch watcher/watcher)
    (Thread/sleep 7000))
  (future
    (config/commandline-overrides! ["--watcher.workers" "10"
                                    "--watcher.worker-poll-ms" "1000"])
    (config/with-env :test (watcher/start (not= "serially" parallel-str)
                                          directory
                                          extensions-str)))
  (Thread/sleep 7000))
