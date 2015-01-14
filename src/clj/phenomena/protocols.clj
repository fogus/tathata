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
  "This protocol is used to provide the minimum required behavior for a
   policy: the determination whether a given action is allowed on a
   pod or not.  It is meant to be called at three different stages of
   a pod's use: retrieval, setting, and snapshotting (rendering)."
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
  (make-pod [sentry val]
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
   pod in a stable state upon independent completion."
  (pod-get-transient [pod]
    "Given a `pod`, this function is expected to return the transient
    representation of its stored object. The argument to this function
    is expected to be valid according to the instance's get precept
    as defined by the pod's policy.")
  (pod-set-transient [pod t]
    "Given a `pod` and an object, this function is expected to set the
    transient version of its stored object. Though the object given is
    likely to be an actual transient object, that is not required. Instead,
    The values given to this function are expected to be valid according
    to the instance's put precept as defined by the pod's policy.")
  (pod-render [pod]
    "Given a `pod`, the `render` function is expected to produce a
    representational value of the contained transient object. The
    rendering is subject to the restrictions dictated by the render
    precept."))
