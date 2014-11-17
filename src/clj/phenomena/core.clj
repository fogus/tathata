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

