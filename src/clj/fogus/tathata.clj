;   Copyright (c) Rich Hickey and Michael Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
(ns fogus.tathata
  "This is the public API for Tathata.  The API is designed to
   present the smallest possible set of functions for dealing with
   pods including:

   - An operation to mutate an ephemeral object contained in a pod.
   - An operation to fetch an ephemeral value from an object contained in a pod.
   - An operation to create a pod based on a policy.
   - An operation to coordinate fetches and mutations based on a policy.

   There's one other operation on pods not directly offered in this
   namespace.  That is, pods provide a mutable object -> value operation
   that may or may not be exposed via the `deref` protocol.  This
   exposure is left to the discretion of pod designers.  See the
   `fogus.tathata.protocols` namespace for more details on
   crafting new pods.

  **NOTE: This project is very much a moving target, so you should expect that
  the API will change from version to version until v1.0.0 is released.**"
  (:require [fogus.tathata.protocols :as proto]))

(def nothing :tathata.core/無)

(defmacro via
  "Calls a method or function `op` through the given `pod` with
   the supplied arguments.  This macro is intended to be used
   for operations that might mutate the object held in the
   pod and guarded / coordinated by the pod's policy."
  [op pod & args]
  `(proto/set-ephemeron
    ~pod
    (~op ~(with-meta `(proto/get-ephemeron ~pod) (meta pod))
        ~@args)))

(defmacro fetch
  "Calls a method or function `op` through the given `pod` with
   the supplied arguments.  This macro is intended to be used
   for operations that read the value of the object held in the
   pod and guarded / coordinated by the pod's policy."
  [op pod & args]
  `(~op ~(with-meta `(proto/get-ephemeron ~pod) (meta pod))
       ~@args))

(defn pod
  "Takes an object and a policy and returns an object having `Suchness`,
   managed by the given policy.  This functions delegates the object's
   creation out to the policy, but will perform some checks to
   ensure the veracity of the incoming object and the resulting
   instance."
  [obj policy]
  {:pre  [obj policy]
   :post [(instance? fogus.tathata.protocols.Suchness %)]}
  (proto/make-pod policy obj))

(defmacro guarded
  "Creates a guarded block used to coordinate one or more pods based
   on the dictates of the policy contained therein.  It's expected
   that the policy used for coordination extends the `Coordinator`
   protocol, otherwise an exception is thrown."
  [pods & body]
  (case (count pods)
    0 `(do ~@body)
    1 `(proto/guard
        (.policy ~@pods)
        (fn ~pods ~@body) ~@pods)
    `(proto/coordinate
      (.policy ~(first pods))        ; Coordination begins with the first pod,
                                     ; but its `Coordinator` should decide if
                                     ; the other pods are compatible with the other
                                     ; given pods.
      (fn ~pods ~@body) [~@pods])))

