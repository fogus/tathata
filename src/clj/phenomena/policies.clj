(ns phenomena.policies
  (:require phenomena.core
            [phenomena.impl.thread-cell :as tc]))

(defrecord SingleThreadedRWAccess [thread]
  phenomena.core/Sentry
  (make-cell [this val] (tc/ThreadCell. this val ::none))

  phenomena.core/Axiomatic
  (precept [_] nil))

