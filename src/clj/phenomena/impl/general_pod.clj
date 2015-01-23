;   Copyright (c) Rich Hickey and Michael Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
(ns phenomena.impl.general-pod    ;; LOL 
  (:require phenomena.protocols))

;; This namespace defines the particulars of a specific kind of
;; pod that is meant to provide a capability similar to that of
;; Clojure's transients.  That is, a `GeneralPod` provides the
;; substrate for which to build a transient-like capability on.
;;
;; The `^:unsynchronized-mutable` is, for lack of a better term
;; a pattern for creating pods.  That is, since pods are meant to
;; hold a transient object there should be some modecum of protection.
;; That modecum is of course just to make the transient and its
;; representational value private properties of the pod itself. Now,
;; how these "assignables" are set is entirely up to the disgression
;; of the pod creator.
;;
;; The `GeneralPod` type should be considered as:
;;
;; 1. A base-level pod capability provider
;; 2. A template for more complex pod implementations
;;
;; A more complex pod is implemented as `LockPod`, though the much
;; of what makes a pod interesting is delegated out to policies.
;;
(deftype GeneralPod [policy ;; every pod has a policy   
                     ^:unsynchronized-mutable val ;; This gens a Java private variable
                     ^:unsynchronized-mutable trans
                     _meta]
  Object
  (equals [this o] (identical? this o))
  (hashCode [this] (System/identityHashCode this))
  (toString [_] (str "#<GeneralPod [" val "]>"))

  clojure.lang.IMeta
  (meta [_] _meta)

  ;; The `deref` is analogous to a render operation
  clojure.lang.IDeref
  (deref [this] (phenomena.protocols/pod-render this))

  phenomena.protocols/Pod
  (pod-get-transient [this]
    (phenomena.protocols/precept-get policy this)
    (when (identical? :phenomena.core/nothing trans)
      (set! trans (phenomena.protocols/value->transient val)))
    trans)
  
  (pod-set-transient [this t]
    (phenomena.protocols/precept-set policy this)
    (set! trans t) this)
  
  (pod-render [this]
    (phenomena.protocols/precept-render policy this)
    (when-not (identical? trans :phenomena.core/nothing)
      (set! val (phenomena.protocols/transient->value trans))
      (set! trans :phenomena.core/nothing))
    val))

