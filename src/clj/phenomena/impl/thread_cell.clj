(ns phenomena.impl.thread-cell
  (:require phenomena.protocols))

(deftype ThreadCell [policy
                     ^:unsynchronized-mutable val
                     ^:unsynchronized-mutable trans]
  Object
  (equals [this o] (identical? this o))
  (hashCode [this] (System/identityHashCode this))
  (toString [_] "") ;; TODO 

  clojure.lang.IMeta
  (meta [_] {}) ;; TODO

  clojure.lang.IDeref
  (deref [this] (phenomena.protocols/cell-render this))

  phenomena.protocols/Cell
  (cell-get-transient [_]
    (assert (phenomena.protocols/precept policy)
            (phenomena.protocols/precept-failure-msg policy))
    (when (identical? :phenomena.core/nothing trans)
      (set! trans (phenomena.protocols/transient-of val)))
    trans)
  
  (cell-set-transient [this t]
    (assert (phenomena.protocols/precept policy)
            (phenomena.protocols/precept-failure-msg policy))
    (set! trans t) this)
  
  (cell-render [_]
    (assert (phenomena.protocols/precept policy)
            (phenomena.protocols/precept-failure-msg policy))
    (when-not (identical? trans :phenomena.core/nothing)
      (set! val (phenomena.protocols/value-of trans))
      (set! trans :phenomena.core/nothing))
    val))

