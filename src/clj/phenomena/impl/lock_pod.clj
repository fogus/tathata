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
  (deref [this]
    (if (.isHeldByCurrentThread lock)
      (phenomena.protocols/pod-render this)
      val))

  phenomena.protocols/Pod
  (pod-get-transient [_]
    (assert (phenomena.protocols/precept-get policy)
            (-> policy phenomena.protocols/precept-failure-msgs :get))
    (when (identical? :phenomena.core/nothing trans)
      (set! trans (phenomena.protocols/transient-of val)))
    trans)
  (pod-set-transient [this t]
    (assert (phenomena.protocols/precept-set policy)
            (-> policy phenomena.protocols/precept-failure-msgs :set))
    (set! trans t) this)
  (pod-render [_]
    (assert (phenomena.protocols/precept-get policy)
            (-> policy phenomena.protocols/precept-failure-msgs :get))
    (when-not (identical? trans :phenomena.core/nothing)
      (set! val (phenomena.protocols/value-of trans))
      (set! trans :phenomena.core/nothing))
    val)

  Object
  (toString [_] "TODO"))

(comment

  (extend-type String
    phenomena.protocols/Editable
    (transient-of [s] (StringBuilder. s)))

  (extend-type StringBuilder
    phenomena.protocols/Transient
    (value-of [sb] (.toString sb)))

  (def lp (LockPod. (java.util.concurrent.locks.ReentrantLock. true) ;; fair?
                    ""
                    :phenomena.core/nothing
                    {}))

  @lp ;;=> ""


)