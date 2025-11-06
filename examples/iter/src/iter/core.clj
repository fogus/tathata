;   Copyright (c) Michael Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
(ns iter.core
  (:require [fogus.kernel.tathata :as tathata]
            [fogus.kernel.tathata.protocols :as proto]
            [fogus.kernel.tathata.policies :as policies]
            [fogus.kernel.tathata.impl.general-pod :as gp]))

;; Consists of three fundamental "iteration" operations:
;;
;; - `has-item?` - Predicate that returns true if the iterator has more items
;;                 to yield. This is analogous to Java's Iterator.hasNext().
;;
;; - `item`      - Returns the current item at the iterator's position without
;;                 advancing. This is a non-mutating read operation. Calling
;;                 `item` multiple times returns the same value.
;;
;; - `move!`     - Advances the iterator to the next position and returns the
;;                 iterator itself.
;;
;; This protocol deliberately separates "getting the current item" from
;; "advancing to the next item" as pods can exploit the separation to
;; control when and how iteration state changes occur.
(defprotocol Iter
  (has-item? [iter])
  (item [iter])
  (move! [iter]))

;; Internal interface for lazy realization of iterator state.
;; Called on every seq operation.
(definterface IterSeqImpl
  (mkseq []))

;; `IterSeq` is a lazy sequence abstraction that wraps an iterator `Iter` stored
;; inside of a pod, managing the transition between mutable iteration state and
;; immutable sequence values. It implements both the `Iter` protocol (exposing
;; iterator operations) and Clojure's `ISeq` interface for navigation.
;;
;; - `policy`: The pod policy controlling iterator access
;; - `iter-pod`: A pod containing the mutable iterator (or nil once realized)
;; - `seq-val`: A cached ISeq value (or nil until realized)
;;
;; The lazy realization happens via `mkseq`, which converts the iterator pod into
;; a concrete ISeq exactly once, caching the result for subsequent accesses.
(deftype IterSeq [policy
                  ^:unsynchronized-mutable iter-pod
                  ^:unsynchronized-mutable ^clojure.lang.ISeq seq-val]

  ;; Implements `Iter` by delegating to the underlying `ISeq` interface. This allows
  ;; an `IterSeq` to be used as an iterator itself, enabling composition of iterator
  ;; operations.
  Iter
  (has-item? [this] (.seq this))
  (item [this] (.first this))
  (move! [this] (.more this))

  ;; Internal implementation of lazy realization. Locks to ensure thread-safety,
  ;; realizes one step of the iterator (fetch current, advance via `move!`), creates
  ;; a new `IterSeq` for the tail, and caches the result as a `Cons` cell.
  IterSeqImpl
  (mkseq [this]
    (locking this
      (when iter-pod
        (set! seq-val
              (when (tathata/fetch has-item? iter-pod)
                (clojure.lang.Cons.
                 (tathata/fetch item iter-pod)
                 (IterSeq. policy (tathata/via move! iter-pod) nil))))
        (set! iter-pod nil))
      seq-val))

  ;; Delegate to `mkseq` for `Seq` view
  clojure.lang.Seqable
  (seq [this] (.mkseq this))

  ;; Implements Clojure's `ISeq` interface, making `IterSeq` a proper lazy sequence.
  ;; All operations call `mkseq` first to ensure realization, then delegate to
  ;; the cached `seq-val`.
  clojure.lang.ISeq
  (first [this]
    (.mkseq this)
    (if (nil? seq-val)
      nil
      (.first seq-val)))

  (more [this]
    (.mkseq this)
    (if (nil? seq-val)
      ()
      (.more seq-val)))

  ;; Implements the pod protocol for converting values to mutable representations.
  ;; Delegates to the appropriate underlying object (either the iterator pod or
  ;; the realized sequence value).
  proto/ToMutable
  (value->mutable [_ policy]
    (proto/value->mutable (if iter-pod @iter-pod seq-val) policy)))

;; Allows unrestricted access to the iterator
(defrecord OpenAccess []
  proto/Sentry
  (make-pod [this val]
    (gp/->GeneralPod this val tathata/nothing {}))

  (compare-pod [this lhs rhs]
    (if (identical? lhs rhs)
      0
      (compare (hash lhs) (hash rhs))))

  proto/Axiomatic
  (precept-get [_ _] true)
  (precept-set [_ _] true)
  (precept-render [_ _] true))

;; Wrap a Clojure sequence as an Iter
(defn seq->iter [s]
  (reify
    Iter
    (has-item? [_] (seq s))
    (item [_] (first s))
    (move! [_] (seq->iter (rest s)))

    proto/ToValue
    (mutable->value [this] this)
    (mutable->value [this sentry] this)

    proto/ToMutable
    (value->mutable [this] this)
    (value->mutable [_ policy] (seq->iter s))))

(defn mapx
  [f coll & {:keys [policy]}]
  (letfn [(iter-walker [f a-pod]
            (reify
              Iter
              (has-item? [_] (tathata/fetch has-item? a-pod))
              (item [_] (f (tathata/fetch item a-pod)))
              (move! [this] (tathata/via move! a-pod) this)

              proto/ToValue
              (mutable->value [this] this)
              (mutable->value [this sentry] this)

              proto/ToMutable
              (value->mutable [this] this)
              (value->mutable [_ policy]
                (iter-walker f (proto/make-pod policy (tathata/fetch identity a-pod))))))]
    (let [it (seq->iter (seq coll))
          policy (or policy (OpenAccess.))
          a-pod (tathata/pod it policy)]
      (IterSeq. policy
                (proto/make-pod policy (iter-walker f a-pod))
                nil))))
