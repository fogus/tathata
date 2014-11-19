(ns phenomena.core)

(defprotocol Editable
  (transient-of [value] [value this]))

(defprotocol Transient
  (value-of [transient] [transient this]))

(defprotocol Axiomatic
  (precept [this]))

(defprotocol Sentry
  (make-cell [sentry val]))

(defprotocol Cell
  (cell-sentry [cell])
  (cell-get-transient [cell])
  (cell-set-transient [cell t])
  (cell-render [cell]))


(defmacro target [f cell & args]
  `(let [f# (fn [tgt#] (~f tgt# ~@args) tgt#)]
     (cell-set-transient ~cell (f# ~(with-meta `(phenomena.core/cell-get-transient ~cell) (meta cell))))))

(defmacro pass [f cell & args]
  `(phenomena.core/cell-set-transient ~cell (~f ~(with-meta `(phenomena.core/cell-get-transient ~cell) (meta cell)) ~@args)))

(defmacro fetch [f cell & args]
  `(~f ~(with-meta `(phenomena.core/cell-get-transient ~cell) (meta cell)) ~@args))

