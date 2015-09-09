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
;; hold an ephemeral object there should be some modecum of protection.
;; That modecum is of course just to make the object and its
;; representational value private properties of the pod itself. Now,
;; how these "assignables" are set is entirely up to the disgression
;; of the pod creator.
;;
;; The `GeneralPod` type should be considered as:
;;
;; 1. A base-level pod capability provider
;; 2. A template for more complex pod implementations
;;
;; A more complex pod is implemented as `LockPod`, though much
;; of what makes a pod interesting is delegated out to policies.
;;
(deftype GeneralPod [policy ;; every pod has a policy   
                     ^:unsynchronized-mutable val ;; This gens a Java private variable
                     ^:unsynchronized-mutable noumenon
                     _meta]
  Object
  (equals [this o] (identical? this o))
  (hashCode [this] (System/identityHashCode this))
  (toString [_] (str "#<GeneralPod:" policy " ["(if val val noumenon) "]>"))

  Comparable
  (compareTo [this o]
    (phenomena.protocols/compare-pod policy this o))
  
  clojure.lang.IMeta
  (meta [_] _meta)

  ;; The `deref` is *always* analogous to a render operation
  clojure.lang.IDeref
  (deref [this] (phenomena.protocols/pod-render this))

  ;; A pod has only three operations: get, set, and render.
  ;; 
  phenomena.protocols/Pod
  ;; A pod is a shell type that holds a mutable object.  While the
  ;; name `noumenon` is used in this context, a "getter" is
  ;; immediately presented to retrieve it...
  ;; ... YOLO
  ;;
  (get-noumenon [this]
    (phenomena.protocols/precept-get policy this)
    (when (identical? :phenomena.core/無 noumenon)
      (set! noumenon (phenomena.protocols/value->mutable val)))
    noumenon)
  
  (set-noumenon [this t]
    (phenomena.protocols/precept-set policy this)
    (set! noumenon t) this)

  ;; Of the three pod operations, `render` is the most interesting.
  ;; That is, pod rendering is the process of turning a mutable
  ;; object into a value.  However, rendering is also a destructive
  ;; process that wipes the noumenal object in favor of the
  ;; value itself.  It's expected that any mutations of the underlying
  ;; object will trigger a `set-noumenon` operations, but of course
  ;; there's no provided way to really enforce that, *except* that the `via` 
  ;; macro enforces that the condition holds. However, if you don't go through
  ;; the `via` macro then there's no guarantee that the noumenal object
  ;; will be what you expect it to be when you expect it to be.
  (pod-render [this]
    (phenomena.protocols/precept-render policy this)
    (when-not (identical? noumenon :phenomena.core/無)
      (set! val (phenomena.protocols/mutable->value noumenon))
      (set! noumenon :phenomena.core/無))
    val))



