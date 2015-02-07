(ns iter.core
  (:require [phenomena.protocols :as pods]
            [phenomena.core :refer (via fetch pod)]))

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

  pods/ToTransient
  (pods/value->transient [_ _] (pods/value->transient (if iter-cell @iter-cell seq-val) policy)))

(defrecord OpenAccess []
  phenomena.protocols/Sentry
  (make-pod [this val trans]
    (->IterSeq this val))

  phenomena.protocols/Axiomatic
  (precept-get [_ _]
    (assert false "You cannot access this pod after construction."))
  (precept-set [_ _]
    (assert false "You cannot access this pod after construction."))
  (precept-render [_ _] true))

#_(defn iter-seq [iter]
  (IterSeq. (cell nil iter) nil))

