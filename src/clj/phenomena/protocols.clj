(ns phenomena.protocols)

(defprotocol Editable
  (transient-of [value] [value this]))

(defprotocol Transient
  (value-of [transient] [transient this]))

(defprotocol Axiomatic
  (precept [this])
  (precept-failure-msg [this]))

(defprotocol Sentry
  (make-cell [sentry val]))

(defprotocol Cell
  (cell-get-transient [cell])
  (cell-set-transient [cell t])
  (cell-render [cell]))
