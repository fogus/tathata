(ns phenomena.protocols)

(defprotocol Editable
  (transient-of [value] [value this]))

(defprotocol Transient
  (value-of [transient] [transient this]))

(defprotocol Axiomatic
  (precept-get [this])
  (precept-set [this])
  (precept-render [this])
  (precept-failure-msgs [this]))

(defprotocol Sentry
  (make-pod [sentry val]))

(defprotocol Pod
  (pod-get-transient [pod])
  (pod-set-transient [pod t])
  (pod-render [pod]))
