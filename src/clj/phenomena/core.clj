(ns phenomena.core
  (:require phenomena.policies))

(defmacro target [f cell & args]
  `(let [f# (fn [tgt#] (~f tgt# ~@args) tgt#)]
     (cell-set-transient ~cell (f# ~(with-meta `(phenomena.core/cell-get-transient ~cell) (meta cell))))))

(defmacro pass [f cell & args]
  `(phenomena.core/cell-set-transient ~cell (~f ~(with-meta `(phenomena.core/cell-get-transient ~cell) (meta cell)) ~@args)))

(defmacro fetch [f cell & args]
  `(~f ~(with-meta `(phenomena.core/cell-get-transient ~cell) (meta cell)) ~@args))

(defn thread-pod
  ([val] ::todo)
  ([val policy] (phenomena.core/make-cell policy val)))