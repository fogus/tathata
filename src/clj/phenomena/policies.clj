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
  (precept-get [_]    (single-threaded? thread))
  (precept-set [_]    (single-threaded? thread))
  (precept-render [_] (single-threaded? thread))
  (precept-failure-msgs [_]
    {:get "You cannot access this pod across disparate threads."
     :set "You cannot access this pod across disparate threads."
     :render "You cannot access this pod across disparate threads."}))

(defrecord ConstructOnly []
  phenomena.protocols/Sentry
  (make-pod [this val]
    (tc/->ThreadPod this val :phenomena.core/nothing {}))

  phenomena.protocols/Axiomatic
  (precept-get [_] false)
  (precept-set [_] false)
  (precept-render [_] false)
  (precept-failure-msgs [_]
    {:get "You cannot access this pod after construction."
     :set "You cannot access this pod after construction."
     :render "You cannot access this pod after construction."}))

(def ^:dynamic *in-cells* nil)

(defrecord ThreadLockPolicy [lock]
  phenomena.protocols/Axiomatic
  (precept-get [_] (.isHeldByCurrentThread lock))
  (precept-set [_] true)
  (precept-render [_] (.isHeldByCurrentThread lock))
  (precept-failure-msgs [_]
    {:get "This lock is held by another thread."
     :render "This lock is held by another thread."})

  phenomena.protocols/Sentry
  (compare-pod [this lhs rhs]
    (assert (identical? this (:policy lhs)) "This policy does not match the LHS pod's policy.")
    (cond (identical? lock (:lock rhs)) 0
          (< (hash lock) (hash (:lock rhs))) -1
          (> (hash lock) (hash (:lock rhs))) 1
          :else (throw (IllegalStateException. (str "Duplicate lock hashes for distinct locks: " lhs " " rhs)))))
  (coordinate [_ fun]
    (assert lock)
    (assert (nil? *in-cells*))
    (binding [*in-cells* true]
      (.lock lock)
      (try
        (fun)
        (finally (.unlock lock)))))
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

(comment
  
        (dotimes [i 10]
          (phenomena.core/pass .append #^StringBuilder lp i))
        @lp


  )