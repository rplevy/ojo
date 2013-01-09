(ns ^:impl ojo.impl.workers
  (:require [ojo.respond :refer :all]
            [clojure.core.incubator :refer [-?>>]]))

(def pending-events (ref (clojure.lang.PersistentQueue/EMPTY)))
(def in-progress (ref #{}))

(defresponse enqueue-events
  "when file-watching is done using parallel workers, this function is used
   as the responder instead of a user-provided responder. This responder
   responds by adding the events to the pending events queue for workers
   to draw from."
  (doseq [evt *events*]
    (dosync (alter pending-events conj evt)))
  {})

(defn drop-event [evt]
  (dosync (alter pending-events pop)))

(defn dispatch-on-next-event [respond eval-composite-hook settings]
  (let [{:keys [file] :as evt} (peek @pending-events)]
    (when evt
      (if (@in-progress file)
        (drop-event evt)
        (do (dosync (alter in-progress conj file)
                    (alter pending-events pop))
            (try
              (-?>> {:events [evt] :settings settings}
                    (eval-composite-hook :before-response)
                    respond
                    (eval-composite-hook :after-response))
              (finally (dosync (alter in-progress disj file)))))))))

;; TO DO: this should support having multiple watches defined that share
;; workers / queue.  For now, for our use case we only support a single watch.

(def active? (atom false))

(defn create-workers
  [{:keys [respond worker-poll-ms eval-composite-hook worker-count settings]
    :as options}]
  (when-not @active? ; single-watch assumption, see above comment
    (reset! active? true)
    (dotimes [worker-id (or worker-count
                            (+ 2 (.. Runtime getRuntime availableProcessors)))]
      (Thread/sleep 100) ; stagger
      (future
        (loop []
          (dispatch-on-next-event respond
                                  eval-composite-hook
                                  (assoc settings :worker-id worker-id))
          (Thread/sleep (or worker-poll-ms 500))
          (when @active? (recur)))))))
