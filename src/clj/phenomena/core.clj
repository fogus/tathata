(ns phenomena.core
  (:require phenomena.protocols
            phenomena.policies))

(defmacro target [f cell & args]
  `(let [f# (fn [tgt#] (~f tgt# ~@args) tgt#)]
     (cell-set-transient ~cell (f# ~(with-meta `(phenomena.protocols/cell-get-transient ~cell) (meta cell))))))

(defmacro pass [f cell & args]
  `(phenomena.protocols/cell-set-transient ~cell (~f ~(with-meta `(phenomena.protocols/cell-get-transient ~cell) (meta cell)) ~@args)))

(defmacro fetch [f cell & args]
  `(~f ~(with-meta `(phenomena.protocols/cell-get-transient ~cell) (meta cell)) ~@args))

(defn pod
  ([val] (pod val (phenomena.policies/->SingleThreadedRWAccess (Thread/currentThread))))
  ([val policy] (phenomena.protocols/make-cell policy val)))

