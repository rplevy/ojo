(ns ojo.respond-test
  (:require [midje.sweet :refer :all]
            [ojo.respond :as base :refer :all]))

(fact
 "an empty events vector will shortcut the -?>> pipeline"
 ((base/response {:events []})
  {}) => nil)

(fact
 "events is not specified in response body, so events pass through untouched"
 ((base/response {:state [{:baz :quux}]})
  {:events [{:foo :bar}]})
 => {:state [{:baz :quux}], :events [{:foo :bar}]})

(fact
 "there is no need to specify passing out passed-in values, it is assumed."
 ((base/response {:state *state* :events *events*})
  {:events [{:foo :bar}] :state [{:baz :quux}]})
 =>
 ((base/response)
  {:events [{:foo :bar}] :state [{:baz :quux}]}))

(fact
 "in the response definition, use the anaphoric bindings as input params."
 ((base/response {:state (repeat 2 (first *state*))
                  :events (repeat 3 (first *events*))})
  {:events [{:foo :bar}]
   :state [{:baz :quux}]})
 =>
 {:events [{:foo :bar} {:foo :bar} {:foo :bar}]
  :state [{:baz :quux} {:baz :quux}]})
