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

;;
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
;;
(defprotocol Iter
  (has-item? [iter])
  (item [iter])
  (move! [iter]))

(definterface IterSeqImpl
  (mkseq []))

(deftype IterSeq [policy
                  ^:unsynchronized-mutable iter-cell
                  ^:unsynchronized-mutable ^clojure.lang.ISeq seq-val]
  Iter
  (has-item? [this] (.seq this))
  (item [this] (.first this))
  (move! [this] (.more this))

  IterSeqImpl
  (mkseq [this]
    (locking this
      (when iter-cell
        (set! seq-val
              (when (tathata/fetch has-item? iter-cell)
                (clojure.lang.Cons.
                 (tathata/fetch item iter-cell)
                 (IterSeq. policy (tathata/via move! iter-cell) nil))))
        (set! iter-cell nil))
      seq-val))

  clojure.lang.Seqable
  (seq [this] (.mkseq this))

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

  proto/ToMutable
  (value->mutable [_ policy]
    (proto/value->mutable (if iter-cell @iter-cell seq-val) policy)))

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
