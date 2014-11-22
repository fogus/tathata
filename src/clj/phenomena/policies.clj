(ns phenomena.policies
  (:require phenomena.protocols
            [phenomena.impl.thread-pod :as tc]))

(defrecord SingleThreadedRWAccess [thread]
  phenomena.protocols/Sentry
  (make-pod [this val] (tc/->ThreadPod this val :phenomena.core/nothing))

  phenomena.protocols/Axiomatic
  (precept-get [_] (identical? (Thread/currentThread) thread))
  (precept-set [_] (identical? (Thread/currentThread) thread))
  (precept-render [_] (identical? (Thread/currentThread) thread))
  (precept-failure-msgs [_]
    {:get "You cannot access this pod across disparate threads."
     :set "You cannot access this pod across disparate threads."
     :render "You cannot access this pod across disparate threads."}))


(defrecord ConstructOnly []
  phenomena.protocols/Sentry
  (make-pod [this val] (tc/->ThreadPod this val :phenomena.core/nothing))

  phenomena.protocols/Axiomatic
  (precept [_] false)
  (precept-failure-msg [_] "You cannot access this pod after construction."))