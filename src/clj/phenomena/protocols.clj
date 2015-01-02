(ns phenomena.protocols)

(defprotocol Editable
  (transient-of [value] [value this]))

(defprotocol Transient
  (value-of [transient] [transient this]))

(defprotocol Axiomatic
  (precept-get [this pod])
  (precept-set [this pod])
  (precept-render [this pod]))

(defprotocol Sentry
  (make-pod [sentry val])
  (compare-pod [sentry lpod rpod])  ;; TODO: Should this take the pod too?
  (coordinate [sentry fun]          ;; TODO: Maybe call this "guard"
              [sentry fun pods]))

(defprotocol Pod
  (pod-get-transient [pod])
  (pod-set-transient [pod t])
  (pod-render [pod]))
