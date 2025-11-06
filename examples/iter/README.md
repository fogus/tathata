# iter

A demonstration project showing how to build lazy iteration abstractions using [Tathata](https://github.com/fogus/tathata) pods.

## Purpose

This example demonstrates how pods can be used to manage mutable iteration state while presenting an immutable lazy sequence interface. It showcases the core concept of pod's separated concerns:

- Iteration state - managed inside the pod
- Access policy - controlled by configuration
- Sequence interface - presented to users

Functional lazy sequences have hidden mutable state and Tathata is used to make this explicit and controllable.

## What this project demonstrates

1. Pods wrap mutable state
2. Access policies are pluggable
3. Decomplecting enables reuse by allowing the same iterator abstraction works with different policies
4. Functional interactions while using efficient mutation internally

## Example

```clojure
(require '[iter.core :as iter])

;; Basic mapping
(iter/mapx inc [1 2 3 4 5])
;;=> (2 3 4 5 6)

;; Lazy evaluation - only realizes what's needed
(take 3 (iter/mapx #(* % %) (range 1000)))
;;=> (0 1 4)
```

## Running Tests

   clj -X:dev:test

## Caveats

This is not a production library. It's intention is to illustrate how to use pods.

## License

Copyright © 2015-2025 Michael Fogus

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
