(ns phenomena.impl.lock-pod
  (:require phenomena.protocols))

(deftype LockCell [lock
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
      (phenomena.protocols/pod-render this)
      val))

  phenomena.protocols/Pod
  (pod-get-transient [_]
    (assert (.isHeldByCurrentThread lock))
    (when (identical? ::none trans)
      (set! trans (phenomena.protocols/transient-of val)))
    trans)
  (pod-set-transient [this t] (set! trans t) this)
  (pod-render [_]
    (assert (.isHeldByCurrentThread lock))
    (when-not (identical? trans ::none)
      (set! val (phenomena.protocols/value-of trans))
      (set! trans ::none))
    val)

  Object
  (toString [_] "TODO"))

