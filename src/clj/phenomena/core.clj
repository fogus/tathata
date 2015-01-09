;   Copyright (c) Rich Hickey and Michael Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
(ns phenomena.core
  "This is the public API for Phenomena.  The API is designed to
   present the smallest possible set of functions for dealing with
   pods including:

   - An operation to mutate a transient object contained in a pod.
   - An operation to fetch a transient value from an object contained in a pod.
   - An operation to create a pod based on a policy.
   - An operation to coordinate fetches and mutations based on a policy."
  (:require [phenomena.protocols :as proto]))

(defmacro via
  "Calls a method or function `op` through the given `pod` with
   the supplied arguments.  This macro is intended to be used
   for operations that might mutate the transient held  in the
   pod and guarded / coordianted by the pod's policy."
  [op pod & args]
  `(proto/pod-set-transient
    ~pod
    (~op ~(with-meta `(proto/pod-get-transient ~pod) (meta pod))
        ~@args)))

(defmacro fetch
  "Calls a method or function `op` through the given `pod` wiht
   the supplied arguments.  This macro is intended to be used
   for operations that read the value of the transient held in the
   pod and guarded / coordinated by the pod's policy."
  [op pod & args]
  `(~op ~(with-meta `(proto/pod-get-transient ~pod) (meta pod))
       ~@args))

(defn pod
  "Takes an object and a policy and returns a pod that will be
   managed by the given policy."
  [obj policy]
  {:pre  [obj policy]
   :post [(instance? phenomena.protocols.Pod %)]}
  (proto/make-pod policy obj))

(defmacro guarded
  ""
  [pods & body]
  (case (count pods)
    0 `(do ~@body)
    1 `(proto/guard
        (.policy ~@pods)
        (fn ~pods ~@body) ~@pods)
    `(proto/coordinate
      (.policy ~(first pods))       ; Coordination begins with the first pod, but its `Coordinator`
                                    ; should decide if the other pods are compatible with the other
                                    ; given pods.
      (fn ~pods ~@body) [~@pods])))

