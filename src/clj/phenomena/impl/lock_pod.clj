(ns phenomena.impl.lock-pod
  (:require phenomena.protocols))

(deftype LockCell [^java.util.concurrent.locks.ReentrantLock lock
                   ^:volatile-mutable val
                   ^:unsynchronized-mutable trans
                   meta]
  Object
  (equals [this o] (identical? this o))
  (hashCode [this] (System/identityHashCode this))

  Comparable
  (compareTo [this o]
    (cond (identical? lock (:lock o)) 0
          (< (hash lock) (hash (:lock o))) -1
          (> (hash lock) (hash (:lock o))) 1
          :else (throw (IllegalStateException. (str "Duplicate lock hashes for distinct locks: " this " " o)))))

  clojure.lang.IMeta
  (meta [_] {}) ;; TODO

  clojure.lang.IDeref
  (deref [this]
    (if (.isHeldByCurrentThread lock)
      (cell-render this)
      val))

  Cell
  (cell-sentry [_] lock)
  (cell-get-transient [_]
    (assert (.isHeldByCurrentThread lock))
    (when (identical? ::none trans)
      (set! trans (core/transient-of val)))
    trans)
  (cell-set-transient [this t] (set! trans t) this)
  (cell-render [_]
    (assert (.isHeldByCurrentThread lock))
    (when-not (identical? trans ::none)
      (set! val (core/value-of trans))
      (set! trans ::none))
    val)

  Object
  (toString [_] "TODO"))

