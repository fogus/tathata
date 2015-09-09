(ns iter.core
  (:require [tathata.protocols :as pods]
            [tathata.core :refer (via fetch pod)]))

(defprotocol Iter
  (has-item [iter])
  (item [iter])
  (move! [iter]))

(definterface IterSeqImpl
  (mkseq []))

(deftype IterSeq [policy
                  #^{:unsynchronized-mutable true} iter-cell
                  #^{:unsynchronized-mutable true :tag clojure.lang.ISeq} seq-val]

  Iter
  (has-item [this] (.seq this))
  (item [this] (.first this))
  (move! [this] (.more this))

  IterSeqImpl
  (mkseq [this]
    (locking this
      (when iter-cell
        (set! seq-val
              (when (fetch has-item iter-cell)
                (clojure.lang.Cons. (fetch item iter-cell) (IterSeq. policy (via move! iter-cell) nil))))
        (set! iter-cell nil))
      seq-val))

  clojure.lang.Seqable
  (seq [this] (.mkseq this))

  clojure.lang.ISeq
  (first [this] (.mkseq this) (if (nil? seq-val) nil (.first seq-val)))
  (more [this] (.mkseq this) (if (nil? seq-val) () (.more seq-val)))

  pods/ToMutable
  (pods/value->mutable [_ _] (pods/value->mutable (if iter-cell @iter-cell seq-val) policy)))

(defrecord OpenAccess []
  tathata.protocols/Sentry
  (make-pod [this val trans]
    (->IterSeq this val trans)) ;; Correct?

  tathata.protocols/Axiomatic
  (precept-get [_ _] true)
  (precept-set [_ _] true)
  (precept-render [_ _] true))

(defn iter-seq [iter]
  (IterSeq. (OpenAccess.)
            (pods/make-pod (OpenAccess.) nil iter)
            nil))

(defn mapx
  ([f coll]
     (letfn [(iter [f seq-cell]
               (reify
                 Iter
                 (has-item [_] (fetch has-item seq-cell))
                 (item [_] (f (fetch item seq-cell)))
                 (move! [this] (via move! seq-cell) this)
                 pods/ToValue
                 (mutable->value [_]
                   (reify pods/ToMutable
                     (pods/value->mutable [_ policy]
                       (iter f (pods/make-pod policy (sequence @seq-cell))))))))]
       (iter-seq (iter f (pod (sequence coll) (OpenAccess.)))))))

(comment

  (mapx inc [1 2 3])

)