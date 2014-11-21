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
    ;; TODO: check precepts
    (when (identical? :phenomena.core/nothing trans)
      (set! trans (phenomena.protocols/transient-of val)))
    trans)
  
  (cell-set-transient [this t] (set! trans t) this)
  
  (cell-render [_]
    ;; TODO: check precepts
    (when-not (identical? trans :phenomena.core/nothing)
      (set! val (phenomena.protocols/value-of trans))
      (set! trans :phenomena.core/nothing))
    val))

