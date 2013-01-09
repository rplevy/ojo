(ns ojo.respond)

(defmacro response [& body]
  `(fn [{~'*events* :events, ~'*state* :state, ~'*settings* :settings
         :as info#}]
     (let [{events# :events, state# :state, settings# :settings
            :as resultant#} (do ~@body)]
       (when (or (nil? events#)  ; not specified => no modification of events
                 (not (empty? events#))) ; empty => nil shortcut -?>> pipeline
         (merge info# resultant#)))))

(defmacro defresponse [name & body] `(def ~name (response ~@body)))

(defn drop-events
  "in a response, setting events to an empty coll cuts further processing short"
  []
  {:events []})

(defn no-updates
  "in a response, not passing any keys means no updates, pass what was input"
  []
  {})
