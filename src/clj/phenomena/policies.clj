(ns phenomena.policies
  (:require phenomena.protocols
            [phenomena.impl.thread-cell :as tc]))

(defrecord SingleThreadedRWAccess [thread]
  phenomena.protocols/Sentry
  (make-cell [this val] (tc/->ThreadCell this val :phenomena.core/nothing))

  phenomena.protocols/Axiomatic
  (precept [_] (identical? (Thread/currentThread) thread))
  (precept-failure-msg [_] "You cannot access this pod across disparate threads."))


(defrecord ConstructOnly [thread]
  phenomena.protocols/Sentry
  (make-cell [this val] (tc/->ThreadCell this val :phenomena.core/nothing))

  phenomena.protocols/Axiomatic
  (precept [_] false)
  (precept-failure-msg [_] "You cannot access this pod after construction."))