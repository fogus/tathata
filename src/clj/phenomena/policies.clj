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