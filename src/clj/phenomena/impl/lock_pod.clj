(ns phenomena.impl.lock-pod
  (:require phenomena.protocols
            phenomena.core))

(deftype LockPod [lock
                  ^:volatile-mutable val
                  ^:unsynchronized-mutable trans
                  _meta]
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
  (meta [_] _meta)

  clojure.lang.IDeref
  (deref [this]
    (if (.isHeldByCurrentThread lock)
      (phenomena.protocols/pod-render this)
      val))

  phenomena.protocols/Pod
  (pod-get-transient [_]
    (assert (.isHeldByCurrentThread lock))
    (when (identical? :phenomena.core/nothing trans)
      (set! trans (phenomena.protocols/transient-of val)))
    trans)
  (pod-set-transient [this t] (set! trans t) this)
  (pod-render [_]
    (assert (.isHeldByCurrentThread lock))
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

  (def ^:dynamic *in-cells* nil)
  
  (let [lock (.lock lp)]
    (assert lock)
    (assert (nil? *in-cells*))
    (binding [*in-cells* true]
      (.lock lock)
      (try
        (dotimes [i 10]
          (phenomena.core/pass .append #^StringBuilder lp i))
        @lp
        (finally (.unlock lock)))))

)