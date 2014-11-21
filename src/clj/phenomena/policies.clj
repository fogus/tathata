(ns phenomena.policies
  (:require phenomena.protocols
            [phenomena.impl.thread-cell :as tc]))

(defrecord SingleThreadedRWAccess [thread]
  phenomena.protocols/Sentry
  (make-cell [this val] (tc/ThreadCell. this val ::none))

  phenomena.protocols/Axiomatic
  (precept [_] nil))

