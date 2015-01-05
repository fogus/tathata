(ns phenomena.policies
  (:require phenomena.protocols
            [phenomena.impl.thread-pod :as tc]))

(def ^:private single-threaded?
  #(identical? (Thread/currentThread) %))

(defn ^:private make-ctor [ctor pod val sentinel]
  (ctor pod val sentinel))

(defrecord SingleThreadedRWAccess [thread]
  phenomena.protocols/Sentry
  (make-pod [this val]
    (tc/->ThreadPod this val :phenomena.core/nothing {}))

  phenomena.protocols/Axiomatic
  (precept-get [_ _]
    (assert (single-threaded? thread)
            "You cannot access this pod across disparate threads."))
  (precept-set [_ _]
    (assert (single-threaded? thread)
            "You cannot access this pod across disparate threads."))
  (precept-render [_ _]
    (assert (single-threaded? thread)
            "You cannot access this pod across disparate threads.")))

(defrecord ConstructOnly []
  phenomena.protocols/Sentry
  (make-pod [this val]
    (tc/->ThreadPod this val :phenomena.core/nothing {}))

  phenomena.protocols/Axiomatic
  (precept-get [_ _]
    (assert false "You cannot access this pod after construction."))
  (precept-set [_ _]
    (assert false "You cannot access this pod after construction."))
  (precept-render [_ _]
    (assert false "You cannot access this pod after construction.")))

(def ^:dynamic *in-cells* nil)

(defrecord ThreadLockPolicy [lock]
  phenomena.protocols/Axiomatic
  (precept-get [this pod]
    (assert (identical? this (.policy pod))
            "Policies are not the same.")
    (assert (.isHeldByCurrentThread lock)
            "This pods requires guarding before access."))
  (precept-set [_ _] true)
  (precept-render [this pod]
    (assert (identical? this (.policy pod))
            "Policies are not the same.")
    (assert (.isHeldByCurrentThread lock)
            "This pod requires guarding before rendering."))

  phenomena.protocols/Sentry
  (compare-pod [this lhs rhs]
    (assert (identical? this (:policy lhs))
            "This policy does not match the LHS pod's policy.")
    (let [rlock (.lock (:policy rhs))]
      (cond (identical? lock rlock) 0
            (< (hash lock) (hash rlock)) -1
            (> (hash lock) (hash rlock)) 1
            :else (throw (IllegalStateException. (str "Duplicate lock hashes for distinct locks: "
                                                      lhs
                                                      " vs. "
                                                      rhs))))))

  phenomena.protocols/Coordinator
  (guard [_ fun pod]
    (assert lock)
    (assert (nil? *in-cells*))
    (binding [*in-cells* true]
      (.lock lock)
      (try
        (fun pod)
        (finally
         (.unlock lock)))))
  (coordinate [_ fun pods]
    (assert (nil? *in-cells*))
    (let [s (java.util.TreeSet. #^java.util.Collection pods)
          unlock-all #(doseq [cell %]
                        (let [lock #^java.util.concurrent.locks.ReentrantLock (:lock cell)]
                          (when (.isHeldByCurrentThread lock) (.unlock lock))))]
      (binding [*in-cells* true]
        (try
          (doseq [cell s]
            (assert (:lock cell))
            (.lock #^java.util.concurrent.locks.ReentrantLock (:lock cell)))
          (apply fun pods)
          (finally
           (unlock-all s)))))))

