# ojo

## Overview

Ojo is a simple and extensible framework for watching files and responding to events. It uses the Java 7 Watch Service API. An extension system enables custom behaviors to be plugged into hooks that are applied at different phases of file-watching.

A parallel event handling worker system is optionally provided for high-performance handling of high-volume file activity (for example, the use case that drove development of this library is the processing of frequent changes in a large number of data files and feeding new data to a message queue).

This code has been deployed in production as the file-watching engine for an application that constantly responds to changes in large amounts of data. It is known to perform well on Windows, Linux, and Mac OS file systems (Mac is known to have longer response latencies due to the lack of native file events).

## Dependency Coordinates

https://clojars.org/ojo

## Usage

TODO: write

### Extension System

TODO: write

### Example Project

Executable usage documentation can be found in the cucumber [features](http://www.example.com "TODO") and [step definitions](http://www.example.com "TODO") for the ```example/``` project included in this repo. Run ```lein cucumber``` to test these example scenarios.

## License

Author: Robert Levy / @rplevy-draker

Copyright © 2013 Draker

Distributed under the Eclipse Public License, the same as Clojure.
