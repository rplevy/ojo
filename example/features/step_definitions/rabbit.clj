(require '[midje.sweet :refer :all]
         '[milieu.config :as config]
         '[clojure.data.json :as json]
         '[com.mefesto.wabbitmq :as mq]
         '[darker-rabbit.core :as queue]
         '[robert.hooke :refer [add-hook clear-hooks]])

(Given #"^rabbit is setup and empty$" []
  (config/with-env :test
    (queue/with-connection
      (queue/setup-queues)
      (queue/with-channel
        (mq/queue-declare "csi.csv.v1.test")
        (mq/exchange-declare (config/value :rabbit-mq :exchange) "topic")
        (mq/queue-bind "csi.csv.v1.test"
                       (config/value :rabbit-mq :exchange)
                       (config/value :rabbit-mq :routing-key))
        (mq/queue-purge "csi.csv.v1.test")))))

(Then #"^the following message should be on the queue:$" [message]
  (config/with-env :test
    (queue/with-connection
      (queue/with-channel
        (mq/with-queue "csi.csv.v1.test"
          (let [{:keys [body envelope]} (first (mq/consuming-seq false 2000))
                body'    (-> body
                           (assoc :version-timestamp 11111111)
                           (update-in [:new-data-event?] str))
                expected (-> (json/read-json message)
                           (assoc :version-timestamp 11111111)
                           (update-in [:new-data-event?] str))]
            (fact
              (doseq [field (keys expected)]
                (select-keys body' [field]) => (select-keys expected [field])))
            (queue/ack (:delivery-tag envelope))))))))

(Then #"^the queue should be empty.$" []
  (config/with-env :test
    (queue/with-connection
      (queue/with-channel
        (mq/with-queue "csi.csv.v1.test"
          (assert (fact (count (mq/consuming-seq false 2000)) => 0)))))))

(defn sabotage [f & args] (throw (Exception. "did not publish. :(")))

(When #"^rabbitmq is broken$" []
  (add-hook #'darker-rabbit.core/publish #'sabotage))

(When #"^rabbitmq is unbroken$" []
  (clear-hooks #'darker-rabbit.core/publish))
