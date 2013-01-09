(ns ^:impl ojo.impl.path
  (:require [fs.core :as fs]
            [clojure.string :as str])
  (:import [java.io File]
           [java.nio.file FileSystems]))

(defn list-dir-paths [path]
  (map #(str (.resolve path %)) (fs/list-dir (str path))))

(defn split-path
  "produce strings in format accepted by Path"
  [path-str]
  (let [[first-piece & rest-of-pieces] (fs/split path-str)]
    [(str first-piece File/separator)
     (str/join File/separator rest-of-pieces)]))

(defn path
  "TODO consider better ways of expressing available ways of specifying paths
   this will likely use java's PathMatcher class"
  [[path & paths]]
  (.getPath (FileSystems/getDefault) path (into-array paths)))

(defn glob-matches-pattern?
  "match pattern using fileglob matching functionality of PathMatcher class"
  [dir file file-glob]
  (.matches (.getPathMatcher (FileSystems/getDefault)
                             (format "glob:%s/%s" dir file-glob))
           (path (split-path file))))

(defn matches-pattern?
  "patterns that are strings are file-globs, otherwise they are regexes.
   pattern group in a vector means it must fit all patterns in the vector."
  [dir file pattern]
  (cond
   (string? pattern) (glob-matches-pattern? dir file pattern)
   (vector? pattern) (every? (partial matches-pattern? dir file) pattern)
   :must-be-regex    (re-find pattern file)))

(defn match?
  "the file must match at least one of the patterns."
  [dir file patterns]
  (some (partial matches-pattern? dir file) patterns))
