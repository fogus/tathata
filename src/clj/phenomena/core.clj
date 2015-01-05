(ns phenomena.core
  (:require phenomena.protocols
            phenomena.policies))

(defmacro pass [f pod & args]
  `(phenomena.protocols/pod-set-transient ~pod (~f ~(with-meta `(phenomena.protocols/pod-get-transient ~pod) (meta pod)) ~@args)))

(defmacro fetch [f pod & args]
  `(~f ~(with-meta `(phenomena.protocols/pod-get-transient ~pod) (meta pod)) ~@args))

(defn pod
  ([val] (pod val (phenomena.policies/->SingleThreadedRWAccess (Thread/currentThread))))
  ([val policy] (phenomena.protocols/make-pod policy val)))

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

