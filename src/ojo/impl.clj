(ns ^:impl ojo.impl
  (:require [ojo.impl
             [path :as path]
             [extension :as extension]
             [workers :as workers]]
            [ojo.respond :refer :all]
            [clojure.core.incubator :refer [-?>>]])
  (:import [java.nio.file FileSystems StandardWatchEventKinds]))

(def key->kind {:create StandardWatchEventKinds/ENTRY_CREATE
                :delete StandardWatchEventKinds/ENTRY_DELETE
                :modify StandardWatchEventKinds/ENTRY_MODIFY})

(def kind->key {"ENTRY_CREATE" :create
                "ENTRY_DELETE" :delete
                "ENTRY_MODIFY" :modify})

(defn register
  "register event kinds and produce a watch key"
  [path watcher kinds-to-watch]
  (.register path watcher (into-array (keep key->kind kinds-to-watch))))

(defn events->info [events path]
  (map (fn [ev] {:kind (kind->key (str (.kind ev)))
                 :file (str (.resolve path (str (.context ev))))})
       events))

(defn await-events
  "produces a seq of maps following the ocurrence of a qualifying event
   each map contains information on a matching file event that occured."
  [watcher path]
  (-> watcher .take ; wait here for events
      .pollEvents seq (events->info path)))

(defn existing-files-as-events [path-obj]
  (map (fn [file-path] {:kind :init :file file-path})
       (path/list-dir-paths path-obj)))

(defn constrain-to-patterns
  "strip out events pertaining to files that don't match specified patterns"
  [dir patterns info]
  ((response {:events (vec (filter #(path/match? dir (:file %) patterns)
                                   *events*))}) info))

;; NOTE: assumption of single watcher for now
(def active? (atom false))

(defn watch
  "wait for an event, perform its associated response, lather/rinse/recur"
  [watcher [dir & patterns] event-kinds
   {:keys [respond eval-composite-hook settings initialized parallel]
    :as options}]
  (let [path-obj (path/path (path/split-path dir))
        _ (when-not initialized
            (eval-composite-hook
             :init {:events (existing-files-as-events path-obj)
                    :settings settings}))
        key (register path-obj watcher event-kinds)
        events (await-events watcher path-obj)]
    (-?>> {:events events :settings settings}
          (constrain-to-patterns dir patterns)
          (eval-composite-hook :before-event)
          ((if parallel workers/enqueue-events respond))
          (eval-composite-hook :after-event))
    (.reset key)
    (when @active? (recur watcher (cons dir patterns) event-kinds
                          (assoc options :initialized true)))))

(defn create-watch
  "create a hash with start/cease callbacks closing over a watch service object
   and an atom to keep track of file states. if parallel, the start function
   creates workers to respond, otherwise initiate to use responder directly."
  [dir-and-patterns event-kinds
   & {:keys [respond parallel worker-poll-ms worker-count extensions settings
             file-states-atom] :as options}]
  (let [watcher-obj (.newWatchService (FileSystems/getDefault))
        file-states-atom (or file-states-atom (atom {}))
        eval-composite-hook (extension/composite-hook-fn
                             extensions file-states-atom)]
    {:start (if parallel
              (fn []
                (reset! active? true)
                (workers/create-workers
                 {:worker-poll-ms worker-poll-ms
                  :worker-count worker-count
                  :eval-composite-hook eval-composite-hook
                  :settings settings
                  :respond respond})
                (watch
                 watcher-obj dir-and-patterns event-kinds
                 {:eval-composite-hook eval-composite-hook
                  :settings settings
                  :parallel true}))
              (fn []
                (reset! active? true)
                (watch
                 watcher-obj dir-and-patterns event-kinds
                 {:eval-composite-hook eval-composite-hook
                  :settings settings
                  :respond respond})))
     :cease (if parallel
              (fn []
                (.close watcher-obj)
                "TODO: support defining multiple watchers that have workers"
                (reset! active? false)
                (reset! workers/active? false))
              (fn []
                (.close watcher-obj)
                (reset! active? false)))}))
