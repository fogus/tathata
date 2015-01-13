;   Copyright (c) Rich Hickey and Michael Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
(ns phenomena.protocols
  "This namespace contains only the protocols used to create and
   extend pods.  All pod and policy operations will occur in
   relation to these protocols, including:

   - Turning a value into a transient reprsentation.
   - Turning a transient into a value.
   - Performing checks to ensure that pod operations can be executed.
   - Sentry operations for pod creation and comparison.
   - Coordination.
   - Pod mutation, fetch, and rendering operations.")

;; TODO: Perhaps add a sequence chart here?

(defprotocol ToTransient
  "This protocol used to extend a value type such that by calling the
   function `value->transient` a transient (i.e. mutable) version of the
   object is returned.  A good example for this protocol is to extend
   Java's `String` type to `ToTransient` whereby a `StringBuffer` or
   perhaps a `StringBuilder` instance is returned."
  (value->transient [value] [value this]))

(defprotocol ToValue
  "This protocol is the dual of the `ToTransient` protocol.  It's meant
   extend a transient type (i.e. mutable) such that by calling the
   `transient->value` function a value type is returned.  A good example for
   this protocol is to extend the `StringBuffer` type to `Transient`
   whereby a `String` instance is returned."
  (transient->value [transient] [transient policy]
   "This function should The `[transient]` form of this function
   is expected to take a transient object (i.e. mutable) and
   return a representational value of it.  The function taking
   a second argument is expected to receive a policy instance that
   can safely turn the transient object into a value."))

(defprotocol Axiomatic
  (precept-get [this pod])
  (precept-set [this pod])
  (precept-render [this pod]))

(defprotocol Sentry
  (make-pod [sentry val])
  (compare-pod [sentry lpod rpod]))

(defprotocol Coordinator
  (guard [sentry fun pod])
  (coordinate [sentry fun pods]))

(defprotocol Pod
  (pod-get-transient [pod])
  (pod-set-transient [pod t])
  (pod-render [pod]))
