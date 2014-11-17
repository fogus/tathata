(ns phenomenon.impl.thread-cell
  (:require phenomenon.core))

(deftype ThreadCell [thread
                     axioms
                     ^:unsynchronized-mutable val
                     ^:unsynchronized-mutable trans]
  Object
  (equals [this o] (identical? this o))
  (hashCode [this] (System/identityHashCode this))
  (toString [_] "") ;; TODO 

  clojure.lang.IMeta
  (meta [_] {}) ;; TODO

  clojure.lang.IDeref
  (deref [this] (cell-render this))

  Cell
  (cell-sentry [_] thread)
  (cell-get-transient [_]
    ;; TODO: check precepts
    (when (identical? ::none trans)
      (set! trans (core/transient-of val)))
    trans)
  (cell-set-transient [this t] (set! trans t) this)
  (cell-render [_]
    ;; TODO: check precepts
    (when-not (identical? trans ::none)
      (set! val (core/value-of trans))
      (set! trans ::none))
    val))