(ns ^:impl ojo.impl.extension
  "The only requirement for an ojo extension is that it provide a hash of hooks
   (all of them are optional). This hash is itself the 'extension'."
  (:require [ojo.respond :refer :all]))

(def ^{:doc "init             consumes/produces hash of file-paths + state.
                              file-paths is a collection of strings.
                              hook evaluates when first registering files.

             before-event,
             after-event      consumes/produces hash of events + state.
                              events is a collection of event hashes.
                              hook evaluates after/before the immediate
                              response to the file event. (Note that in
                              parallel mode, this means before/after queuing
                              events for workers to process).

             before-response,
             after-response   consumes/produces hash of events + state.
                              hook evaluates after/before the response
                              to an individual event by a worker.
                              This is only invoked in parallel mode."}
  default-hooks
  {:init            (response)
   :before-event    (response)
   :after-event     (response)
   :before-response (response)
   :after-response  (response)})

(defn compose-hooks
  "takes a collection of extensions (hook hash-maps)
   produces a composite extension."
  [extensions]
  (reduce
   (fn [result extension]
     (reduce (fn [result' hook-key]
               (assoc result' hook-key
                      (comp (or (hook-key extension) identity)
                            (hook-key result'))))
             result
             (keys result)))
   default-hooks
   extensions))

(defn hook
  "given a composite extension evaluate the specified hook.
   the hook receives the present state.
   the value produced by the hook becomes the new state."
  [file-states-atom composite-extension hook-key info]
  (let [result (atom {}) #_"a way of setting only the state part in the
                            atomic swap for file state, while hanging onto
                            the full result of the hook evaluation."]
    (swap! file-states-atom
           (fn [fs] (let [{:keys [state] :as m}
                          ((composite-extension hook-key)
                           (assoc info :state fs))]
                      (reset! result m)
                      (or state fs)))) ; when nil (for shortcutting) don't save
    @result))

(defn composite-hook-fn
  "takes a collection of extensions (hook hash-maps) and the file-states-atom.
   produces a function that evaluates a composite hook"
  [extensions file-states-atom]
  (partial hook file-states-atom (compose-hooks extensions)))
