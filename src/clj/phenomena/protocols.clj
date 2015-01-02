(ns phenomena.protocols)

(defprotocol Editable
  (transient-of [value] [value this]))

(defprotocol Transient
  (value-of [transient] [transient this]))

(defprotocol Axiomatic  ;; TODO: These should take the pod too, recomplect the failure msgs
  (precept-get [this])
  (precept-set [this])
  (precept-render [this])
  (precept-failure-msgs [this]))

(defprotocol Sentry
  (make-pod [sentry val])
  (compare-pod [sentry lpod rpod])  ;; TODO: Should this take the pod too?
  (coordinate [sentry fun]          ;; TODO: Maybe call this "guard"
              [sentry fun pods]))

(defprotocol Pod
  (pod-get-transient [pod])
  (pod-set-transient [pod t])
  (pod-render [pod]))
