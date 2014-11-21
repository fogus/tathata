(ns phenomena.impl.thread-cell
  (:require phenomena.core))


(deftype ThreadCell [recipe
                     ^:unsynchronized-mutable val
                     ^:unsynchronized-mutable trans]
  Object
  (equals [this o] (identical? this o))
  (hashCode [this] (System/identityHashCode this))
  (toString [_] "") ;; TODO 

  clojure.lang.IMeta
  (meta [_] {}) ;; TODO

  clojure.lang.IDeref
  (deref [this] (phenomena.core/cell-render this))

  phenomena.core/Cell
  (cell-get-transient [_]
    ;; TODO: check precepts
    (when (identical? ::none trans)
      (set! trans (phenomena.core/transient-of val)))
    trans)
  
  (cell-set-transient [this t] (set! trans t) this)
  
  (cell-render [_]
    ;; TODO: check precepts
    (when-not (identical? trans ::none)
      (set! val (phenomena.core/value-of trans))
      (set! trans ::none))
    val))

