# ojo

## Overview

Ojo is a simple and extensible framework for watching files and responding to events. It uses the Java 7 Watch Service API. An extension system enables custom behaviors to be plugged into hooks that are applied at different phases of file-watching.

A parallel event handling worker system is optionally provided for high-performance handling of high-volume file activity (for example, the use case that drove development of this library is the processing of frequent changes in a large number of data files and feeding new data to a message queue).

This code has been deployed in production as the file-watching engine for an application that constantly responds to changes in large amounts of data. It is known to perform well on Windows, Linux, and Mac OS file systems (Mac is known to have longer response latencies due to the lack of native file events).

## Dependency Coordinates

https://clojars.org/ojo

## Usage

### defwatch

To watch file activity in a directory, create a watcher using ```defwatch```.

```clojure
  (defwatch watcher
    ["../my/dir/" [["*goodstring.dat" #"^\S+$"]]] [:create :modify]
    {:parallel parallel?
     :worker-poll-ms 1000
     :worker-count 10
     :extensions [throttle track-appends]
     :settings {:throttle-period (config/value :watcher :throttle-period)}}
    (let [[{:keys [file kind appended-only? bit-position] :as evt}]
          *events*]
      (reset! result
              (format "%s%s"
                      (slurp file)
                      (if appended-only? "(append-only)" "")))))
```

Features

*directory*: specify the directory to watch.

*pattern-matching of file names*: file glob and regular expression methods of pattern matching are both supported and can be used interchangeable as demonstrated above. The pattern-matching clause is a vector of vectors. Items within each of these vectors are interpreted with AND logic. The contraints as made distinct by being in separate vectors are interpreted with OR logic. So if the pattern class were

```[["*abc*" #"^f"] [#"^m"]]```

this would be interpreted as "it must contain abc and start with f, OR it must start with m (and it doesn't need abc in the file name in that case)"

*event kinds*: supported kinds are ```:create```, ```:modify```, and ```:delete```.

*options*

* parallel: specify whether to run in serial or parallel mode.
* worker-poll-ms: (parallel mode) workers check the work queue at this frequency
* worker-count: (paralllel mode) how many workers to create
* extensions: a vector of extensions, see below for more info
* settings: a map that can be intialized with any settings to have handy. In the example above :throttle-period is a setting used by the throttle extension.

### event handling

Code defined in extensions and the in the body of defwatch is carried out by means of a simple system for processing the stream of events.  The advantage of this approach to event handling is that is completely functional and very cleanly modular.

Information is passed to a handler via parameters exposed as *events*, *state*, and *settings* which are hash-maps. Updates to these hash-maps can be passed down the pipeline by evaluating to a hash-map containing any or all of the keys :events, :state, and, :settings.

eg.

```clojure
  (defwatch
    ...
    {:state (assoc *state* :some-key "some value")})
```

If the handler body does not evaluate to a hash-map containing any of these 3 keys, then the same input that came into this handler will be passed on down the pipeline.

To *cut short* the event stream for a particular event, pass ```{:events []}```.  As a convenience ojo.respond provides the functions ```drop-events`` which does just this.

### extensions

A collection of ready-made extensions can be found in the project [ojo.extension](https://github.com/drakerlabs/ojo.extension) and are demoed in the example project (see section below).

Extensions use the same event handling logic that is used in defwatch. See the section titled "event handling" for more information.

The available hooks that extensions may run code in include :init (when setting up), :before-event / :after-event (applies to both serial and parallel modes of operation (in the parallel case before handling is done by a worker and after respectively), and :before-response / :after-response (only applies to the parallel mode of operation, executing in the body of code run by the worker, before and after respectively).

The idea of the "before" handlers is to provide a way of doing some processing and making information available in the body of the handler.

Extensions are composed and applied in the order they are specified in in the defwatch macro. At each hook time the relevant extensions are applied in that order.

*implementing extensions*

Extensions are implemented using the response macro defined in ojo.respond. The function produced by this macro must be associated as the value for the key of a hook.

```clojure
(def my-extension
  {:before-event (response
                   ... do some things ...
                   {:events (assoc *events* :random-val (rand 10))})
   :after-event  (response ...)})
```

Remember that the response functions are composable...

```clojure
(def my-extension
  {:before-event (comp (response ... )
                       (response ... ))})
```

For clarity's sake ojo.respond also provides the defresponse macro:

```clojure
(defresponse last-event-state
  {:state (reduce
           (fn [new-state {file :file}]
             (update-in new-state [file]
                        #(assoc % :last-event (now))))
           *state*
           *events*)})

(def my-extension
  {:before-event last-event-state})
```

### a picture of the event-handling pipeline:

```text

serial mode
               +---------at time of handling----------+
   init         before-event   response    after-event
 +----------+  +----------+  +----------+  +----------+
 | settings |  | settings |  | settings |  | settings |
 | state    |->| state    |->| state    |->| state    |
 | events   |  | events   |  | events   |  | events   |
 +---------+-  +----------+  +----------+  +----------+

parallel mode

               +---- time of work-queuing ----+
   init         before-event        after-event
 +----------+  +----------+        +----------+
 | settings |  | settings |        | settings |
 | state    |->| state    |->    ->| state    |
 | events   |  | events   |  queue | events   |
 +---------+-  +----------+   for  +----------+
                            workers
                            \/  /\
                      at time of handing
        +--------------------------------------------+
        |   before-                     after-       |
        |   response      response      response     |
        |  +----------+  +----------+  +----------+  |
        |  | settings |  | settings |  | settings |  |
        |->| state    |->| state    |->| state    |->|
        |  | events   |  | events   |  | events   |  |
        |  +----------+  +----------+  +----------+  |
        +--------------------------------------------+
```

### Example Project

Executable usage documentation can be found in the cucumber [features](.ttps://github.com/drakerlabs/ojo/tree/master/example/features) and [step definitions](https://github.com/drakerlabs/ojo/tree/master/example/features/step_definitions) for the ```example/``` project included in this repo. Run ```lein cucumber``` to test these example scenarios.

## License

Author: Robert Levy / @rplevy-draker

Copyright © 2013 Draker

Distributed under the Eclipse Public License, the same as Clojure.
