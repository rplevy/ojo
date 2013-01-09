(require '[midje.sweet :refer :all]
         '[fs.core :as fs]
         '[example.watcher :as watcher])

(import 'java.io.File
        'org.apache.commons.io.FileUtils)

(Given #"^the directory \"([^\"]*)\" is empty" [directory]
  (fs/delete-dir directory)
  (fs/mkdirs directory))

(When #"^the following data is (written|appended) to \"([^\"]*)\":$"
  [write-type file_name contents]
  (spit file_name (str contents "\n") :append (= write-type "appended"))
  (config/with-env :test (Thread/sleep
                          (config/value| [:cucumber-filewatch-sleep] 20000))))
