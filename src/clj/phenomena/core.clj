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

