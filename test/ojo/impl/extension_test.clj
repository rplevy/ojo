(ns ojo.impl.extension-test
  (:require [midje.sweet :refer :all]
            [ojo.impl.extension :as base]))

(facts
 (map? (base/compose-hooks [])) => true
 (keys (base/compose-hooks [])) => [:init :before-event :after-event
                                    :before-response :after-response]
 (base/compose-hooks [{:init (fn [events-states-settings]
                               (assoc-in events-states-settings
                                         [:state "somefile" :size] 2))}])
 =>
 #(= ((:init %) {:events [] :state {} :settings {}})
     {:events [] :state {"somefile" {:size 2}} :settings {}})

 (base/compose-hooks [{:init (fn [events-states-settings]
                               (assoc-in events-states-settings
                                         [:state "somefile" :size] 2))
                       :before-event
                       (fn [events-states-settings]
                         (assoc-in events-states-settings
                                   [:state "someotherfile" :size] 3))}
                      {:before-event
                       (fn [events-states-settings]
                         (assoc-in events-states-settings
                                   [:state "anotherfile" :size] 4))}])
 =>
 #(= ((:before-event %) {:events []
                         :state {"thisfile" {:size 7}}
                         :settings {}})
     {:events []
      :state {"thisfile" {:size 7}
              "someotherfile" {:size 3}
              "anotherfile" {:size 4}}
      :settings {}}))
