(ns ojo.respond)

(defmacro response [& body]
  `(fn [{~'*events* :events, ~'*state* :state, ~'*settings* :settings
         :as info#}]
     (let [evaluated-to*# (do ~@body)
           evaluated-to# (if (map? evaluated-to*#)
                           (select-keys evaluated-to*#
                                        [:events :state :settings])
                           {})
           {events# :events, state# :state, settings# :settings} evaluated-to#]
       (when (or (nil? events#)  ; not specified => no modification of events
                 (not (empty? events#))) ; empty => nil shortcut -?>> pipeline
         (merge info# evaluated-to#)))))

(defmacro defresponse [name & body] `(def ~name (response ~@body)))

(defn drop-events
  "in a response, setting events to an empty coll cuts further processing short"
  []
  {:events []})
