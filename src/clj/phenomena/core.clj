;   Copyright (c) Rich Hickey and Michael Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
(ns phenomena.core
  (:require phenomena.protocols
            phenomena.policies))

(defmacro pass [f pod & args]
  `(phenomena.protocols/pod-set-transient ~pod (~f ~(with-meta `(phenomena.protocols/pod-get-transient ~pod) (meta pod)) ~@args)))

(defmacro fetch [f pod & args]
  `(~f ~(with-meta `(phenomena.protocols/pod-get-transient ~pod) (meta pod)) ~@args))

(defn pod [val policy]
  (phenomena.protocols/make-pod policy val))

(defmacro guarded [pods & body]
  (case (count pods)
    0 `(do ~@body)
    1 `(phenomena.protocols/guard
        (.policy ~@pods)
        (fn ~pods ~@body) ~@pods)
    `(phenomena.protocols/coordinate
      (.policy ~(first pods))       ; Coordination begins with the first pod, but its `Coordinator`
                                    ; should decide if the other pods are compatible with the other
                                    ; given pods.
      (fn ~pods ~@body) [~@pods])))

