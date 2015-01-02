(ns phenomena.impl.lock-pod
  (:require phenomena.protocols
            phenomena.core))

(deftype LockPod [policy
                  ^:volatile-mutable val
                  ^:unsynchronized-mutable trans
                  _meta]
  Object
  (equals [this o] (identical? this o))
  (hashCode [this] (System/identityHashCode this))

  Comparable
  (compareTo [this o]
    (phenomena.protocols/compare-pod policy this o))

  clojure.lang.IMeta
  (meta [_] _meta)

  clojure.lang.IDeref
  (deref [this] (phenomena.protocols/pod-render this))

  phenomena.protocols/Pod
  (pod-get-transient [this]
    (phenomena.protocols/precept-get policy this)
    (when (identical? :phenomena.core/nothing trans)
      (set! trans (phenomena.protocols/transient-of val)))
    trans)
  (pod-set-transient [this t]
    (phenomena.protocols/precept-set policy this)
    (set! trans t) this)
  (pod-render [this]
    (phenomena.protocols/precept-render policy this)
    (when-not (identical? trans :phenomena.core/nothing)
      (set! val (phenomena.protocols/value-of trans))
      (set! trans :phenomena.core/nothing))
    val)

  Object
  (toString [_] "TODO"))



