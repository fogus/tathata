(ns iter.core
  (:require [fogus.kernel.tathata :as tathata]
            [fogus.kernel.tathata.protocols :as proto]
            [fogus.kernel.tathata.policies :as policies]
            [fogus.kernel.tathata.impl.general-pod :as gp]))

(defprotocol Iter
  (has-item [iter])
  (item [iter])
  (move! [iter]))

(definterface IterSeqImpl
  (mkseq []))

(deftype IterSeq [policy
                  ^:unsynchronized-mutable iter-cell
                  ^:unsynchronized-mutable ^clojure.lang.ISeq seq-val]
  Iter
  (has-item [this] (.seq this))
  (item [this] (.first this))
  (move! [this] (.more this))

  IterSeqImpl
  (mkseq [this]
    (locking this
      (when iter-cell
        (set! seq-val
              (when (tathata/fetch has-item iter-cell)
                (clojure.lang.Cons.
                  (tathata/fetch item iter-cell)
                  (IterSeq. policy (tathata/via move! iter-cell) nil))))
        (set! iter-cell nil))
      seq-val))

  clojure.lang.Seqable
  (seq [this] (.mkseq this))

  clojure.lang.ISeq
  (first [this] (.mkseq this) (if (nil? seq-val) nil (.first seq-val)))
  (more [this] (.mkseq this) (if (nil? seq-val) () (.more seq-val)))

  proto/ToMutable
  (value->mutable [_ policy]
    (proto/value->mutable (if iter-cell @iter-cell seq-val) policy)))

(defrecord OpenAccess []
  proto/Sentry
  (make-pod [this val]
    (gp/->GeneralPod this val :tathata.core/無 {}))

  (compare-pod [this lhs rhs]
    (if (identical? lhs rhs)
      0
      (compare (hash lhs) (hash rhs))))

  proto/Axiomatic
  (precept-get [_ _] true)
  (precept-set [_ _] true)
  (precept-render [_ _] true))

(defn iter-seq [iter]
  (let [policy (OpenAccess.)]
    (IterSeq. policy
              (proto/make-pod policy iter)
              nil)))

(defn seq->iter [s]
  (reify
    Iter
    (has-item [_] (seq s))
    (item [_] (first s))
    (move! [_] (seq->iter (rest s)))

    proto/ToValue
    (mutable->value [this] this)
    (mutable->value [this sentry] this)

    proto/ToMutable
    (value->mutable [this] this)
    (value->mutable [_ policy] (seq->iter s))))

(defn mapx
  [f coll]
  (letfn [(make-iter [f seq-cell]
            (reify
              Iter
              (has-item [_] (tathata/fetch has-item seq-cell))
              (item [_] (f (tathata/fetch item seq-cell)))
              (move! [this] (tathata/via move! seq-cell) this)

              proto/ToValue
              (mutable->value [this] this)
              (mutable->value [this sentry] this)

              proto/ToMutable
              (value->mutable [this] this)
              (value->mutable [_ policy]
                (make-iter f (proto/make-pod policy (tathata/fetch identity seq-cell))))))]
    (iter-seq (make-iter f (tathata/pod (seq->iter (sequence coll)) (OpenAccess.))))))
