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

   - Turning a value into a mutable reprsentation and back again.
   - Performing checks to ensure that pod operations can be executed.
   - Sentry operations for pod creation and comparison.
   - Coordination.
   - Pod mutation, fetch, and rendering operations.")

;; TODO: Perhaps add a sequence chart here?

(defprotocol ToMutable
  "This protocol used to extend a value type such that by calling the
   function `value->mutable` a mutable version of the
   object is returned.  A good example for this protocol is to extend
   Java's `String` type to `ToMutable` whereby a `StringBuffer` or
   perhaps a `StringBuilder` instance is returned."
  (value->mutable [value] [value this]))

(defprotocol ToValue
  "This protocol is the dual of the `ToMutable` protocol.  It's meant to
   extend a mutable type (including Clojure's transients) such that by calling the
   `mutable->value` function a value type is returned.  A good example for
   this protocol is to extend the `StringBuffer` type to `ToValue`
   whereby a `String` instance is returned."
  (mutable->value [mutable] [mutable sentry]
   "The `[mutable]` form of this function is expected to take a
   mutable object (including Clojure's transients) and
   return a representational value of it.  The function taking
   a second argument is expected to receive a `Sentry` instance that
   can safely guide the conversion of the mutable into a value."))

;; TODO: refine the wording of the [trans sentry] docstring

(defprotocol Axiomatic
  "This protocol is used to provide the minimum required behavior for a
   *policy*: the determination whether a given action is allowed on a
   pod or not.  It is meant to be called at three different stages of
   a pod's use: retrieval, setting, and rendering."
  (precept-get [this pod]
    "Given a pod, determine if a value retrieval is allowed.")
  (precept-set [this pod]
    "Given a pod, determine if assignment is allowed")
  (precept-render [this pod]
    "Given a pod, determine if a snapshot can be built."))

(defprotocol Sentry
  "Some pods will require guarded access or careful instantiation. In these
   cases it's expected that this protocol will be responsible for the
   careful logic around certain guarded tasks.  Very often the policies
   will take on the role of the `Sentry` but that is not a requirement."
  (make-pod [sentry val] [sentry val mutable]
    "This function is tasked with building a pod based on the sentry
    type and the value given. The type of the pod returned is dependent
    on the dictates of the `sentry` type.")
  (compare-pod [sentry lpod rpod]
    "Tasked with comparing two pods for equality. The `sentry` type is
    responsible for the entire equality semantics including, but not
    limited to the pod types, value types, and policy types."))

(defprotocol Coordinator
  "If a pod requires special coordination to access (read or write)
   then this protocol is expected to come into play.  Additionally,
   aside from singleton access, it's conceivable that more than one
   pod will need to be coordinated as well."
  (guard [sentry fun pod]
    "This function is meant to encapsulate the access logic for a
    single pod. It's conceivable that the `guard` logic might be
    constituent to the coordination logic around multiple pods,
    but this is not a requirement. `guard` will receive a function
    `fun` that is meant to receive the `pod` as its only argument.")
  (coordinate [sentry fun pods]
    "This function is meant to coordinate the access of more than
    one pod. The `coordinate` implementation will receive a function
    `fun` and a sequence of `pods`.  The pods in the sequence should
    be given as arguments to the given function. It's left to the
    specific implementations of `coordinate` to define if the `pods`
    given are mutually compatible.  Likewise, the implementation may
    choose to build on the `guard` functionality, but that is not a
    requirement."))

(defprotocol Pod
  "The `Pod` protocol represents the fine-grained pod access logic
   along the get, set, and rendering logics.  These functions are
   meant to operate orthogonally, but are expected to leave the
   pod in a stable state upon completion.  The *noumenon* is the
   object that is contained in the pod and that should not be
   accessed except through the pod itself or the supporting macros."
  (get-noumenon [pod]
    "Given a `pod`, this function is expected to return the mutable
    representation of its stored object, the noumenon. The argument
    to this function is expected to be valid according to the
    instance's get precept as defined by the pod's policy, where
    appropriate.")
  (set-noumenon [pod mutable]
    "Given a `pod` and an object, this function is expected to set the
    mutable version of its stored object, the noumenon. Though the object
    given is likely to be an actual mutable object, that is not required.
    Indeed, the object given could be another pod.  In any case, the argument
    to this function are expected to be valid according to the instance's
    put precept as defined by the pod's policy, where appropriate.")
  (pod-render [pod]
    "Given a `pod`, the `render` function is expected to produce a
    representational value of the contained noumenon. The rendering
    is subject to the restrictions dictated by the render precept, where
    appropriate.")

  (mutant? [pod]
    "Returns `true` or `false` depending if the object in the
    pod has been mutated through the pod."))
