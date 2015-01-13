;   Copyright (c) Rich Hickey and Michael Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
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
    (phenomena.protocols/precept-get policy this)
    (when (identical? :phenomena.core/nothing trans)
      (set! trans (phenomena.protocols/value->transient val)))
    trans)
  
  (pod-set-transient [this t]
    (phenomena.protocols/precept-set policy this)
    (set! trans t) this)
  
  (pod-render [this]
    (phenomena.protocols/precept-render policy this)
    (when-not (identical? trans :phenomena.core/nothing)
      (set! val (phenomena.protocols/transient->value trans))
      (set! trans :phenomena.core/nothing))
    val))

