(require '[midje.sweet :refer :all]
         '[darker-watcher.csi.reset :as reset]
         '[milieu.config :as config]
         '[taoensso.carmine :as carmine])

(Given #"^redis is empty$" []
  (reset/reset-redis))

(def pool (carmine/make-conn-pool :test-while-idle? true))

(def server (config/with-env :test
              (carmine/make-conn-spec
               :host (config/value :redis :host)
               :port (config/value :redis :port)
               :password (config/value| :redis :password)
               :timeout (config/value :redis :timeout))))

(defn carm-eval [f args] (carmine/with-conn pool server (apply f args)))

(Then #"^redis checksum and bit-position for \"([^\"]*)\" are:$"
  [file-name file-state]
  (let [file-key (format "darker-watcher:csi:test:file-state:%s" file-name)
        selected-keys [:checksum :bit-position]]
    (assert
     (fact (-> (first (carm-eval carmine/mget [file-key]))
               (select-keys selected-keys))
           =>
           (-> (read-string file-state)
               (select-keys selected-keys))))))


