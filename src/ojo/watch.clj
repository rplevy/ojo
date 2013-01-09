(ns ojo.watch
  "A library for watching files and reacting to changes,
   implemented atop Java's platform-independent Watch Service API."
  (:require [ojo
             [impl :refer :all]
             [respond :as respond :refer [response]]]
            [useful.map :refer [assoc-or]]))

(defmacro defwatch
  [name dir [& event-kinds] arg-map & body]
  (let [respond `(response ~@body)
        args (mapcat vec (assoc-or arg-map :respond respond))]
  `(def ~name (create-watch ~dir [~@event-kinds] ~@args))))

(defn cease-watch [watch]
  (if-let [cease-watch (:cease watch)]
    (cease-watch)
    (throw (Exception. "No such watcher."))))

(defn start-watch [watch]
  (if-let [start-watch (:start watch)]
    (try
      (start-watch)
      (finally (cease-watch watch)))
    (throw (Exception. "No such watcher."))))

(def drop-events respond/drop-events)
