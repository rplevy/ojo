(ns ojo.impl-test
  (:require [midje.sweet :refer :all]
            [ojo.impl
             [path :as path]
             [extension :as extension]
             [workers :as workers]]
            [ojo.impl :as base])
  (:import [java.nio.file FileSystems WatchKey]))


(let [dir "resources/ojo_test/"]
   (facts
    (base/register (path/path (path/split-path dir))
                   (.newWatchService (FileSystems/getDefault))
                   [:create :modify :delete])
    => #(isa? (type %) WatchKey) (comment "; LinuxWatckKey, etc.")))
