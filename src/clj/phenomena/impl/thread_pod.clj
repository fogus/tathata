(ns phenomena.impl.thread-pod
  (:require phenomena.protocols))

(deftype ThreadPod [policy
                    ^:unsynchronized-mutable val
                    ^:unsynchronized-mutable trans
                    _meta]
  Object
  (equals [this o] (identical? this o))
  (hashCode [this] (System/identityHashCode this))
  (toString [_] (str "#<ThreadPod [" val "]>"))

  clojure.lang.IMeta
  (meta [_] _meta)

  clojure.lang.IDeref
  (deref [this] (phenomena.protocols/pod-render this))

  phenomena.protocols/Pod
  (pod-get-transient [this]
    (assert (phenomena.protocols/precept-get policy this)
            (-> policy phenomena.protocols/precept-failure-msgs :get))
    (when (identical? :phenomena.core/nothing trans)
      (set! trans (phenomena.protocols/transient-of val)))
    trans)
  
  (pod-set-transient [this t]
    (assert (phenomena.protocols/precept-set policy this)
            (-> policy phenomena.protocols/precept-failure-msgs :set))
    (set! trans t) this)
  
  (pod-render [this]
    (assert (phenomena.protocols/precept-render policy this)
            (-> policy phenomena.protocols/precept-failure-msgs :render))
    (when-not (identical? trans :phenomena.core/nothing)
      (set! val (phenomena.protocols/value-of trans))
      (set! trans :phenomena.core/nothing))
    val))


