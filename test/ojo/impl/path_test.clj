(ns ojo.impl.path-test
  (:require [midje.sweet :refer :all]
            [ojo.impl.path :as base]))

(facts
 "the file must match at least one of the patterns."
 (base/match? "./resources" "./resources/fifteenMin.dat"
              [["*fifteenMin*.dat" #"^\S+$"]])
 => truthy

 (base/match? "./resources" "./resources/fifteenMn.dat"
              [["*fifteenMin*.dat" #"^\S+$"]])
 => falsey

 (base/match? "./resources" "./resources/fifteen Min.dat"
              [["*fifteenMin*.dat" #"^\S+$"]])
 => falsey

 (fact
  "if another clause is added it can match"
 (base/match? "./resources" "./resources/fifteen Min.dat"
              [["*fifteenMin*.dat" #"^\S+$"] "*.dat"])
 => truthy))

(facts
 "matching is independent of files on disk, doesn't look at the filesystem"
 (base/match? "/tmp" "/tmp/foo.dat" ["*.dat"]) => truthy
 (base/match? "/tmp" "/tmp/foo.dat" ["*.dat" #"bar"]) => truthy
 (base/match? "/tmp" "/tmp/foo.dat" [["*.dat" #"foo"] #"bar"]) => truthy
 (base/match? "/tmp" "/tmp/foo.dat" [["*.dat" #"bar"]]) => falsey
 (base/match? "/tmp" "/tmp/foo.dat" [#"foo"]) => truthy
 (base/match? "/tmp" "/tmp/foo.dat" [#"bar"]) => falsey
 (base/match? "/tmp" "/tmp/foo.dat" [[#"foo" #"bar"]]) => falsey)

(facts
 "fileglob matching is sensitive to correctness of path,
  whereas our approach to regex matching is by design more general"
 (base/match? "/tmp" "/thewrongdir/foo.dat" ["*.dat"]) => falsey
 (base/match? "/tmp" "/thewrongdir/foo.dat" [#"dat"]) => truthy)
