(ns phenomena.impl.thread-cell
  (:require phenomena.core))

(deftype ThreadCell [thread
                     axioms
                     ^:unsynchronized-mutable val
                     ^:unsynchronized-mutable trans]
  Object
  (equals [this o] (identical? this o))
  (hashCode [this] (System/identityHashCode this))
  (toString [_] "") ;; TODO 

  clojure.lang.IMeta
  (meta [_] {}) ;; TODO

  clojure.lang.IDeref
  (deref [this] (phenomena.core/cell-render this))

  phenomena.core/Cell
  (cell-sentry [_] (phenomena.core/cell-sentry axioms))
  
  (cell-get-transient [_]
    ;; TODO: check precepts
    (when (identical? ::none trans)
      (set! trans (phenomena.core/transient-of val)))
    trans)
  
  (cell-set-transient [this t] (set! trans t) this)
  
  (cell-render [_]
    ;; TODO: check precepts
    (when-not (identical? trans ::none)
      (set! val (phenomena.core/value-of trans))
      (set! trans ::none))
    val))

(extend-protocol phenomena.core/Sentry
  java.lang.Thread
  (make-cell [thread val] (ThreadCell. thread nil val ::none)))

(defmacro target [f cell & args]
  `(let [f# (fn [tgt#] (~f tgt# ~@args) tgt#)]
     (cell-set-transient ~cell (f# ~(with-meta `(phenomena.core/cell-get-transient ~cell) (meta cell))))))

(defmacro pass [f cell & args]
  `(phenomena.core/cell-set-transient ~cell (~f ~(with-meta `(phenomena.core/cell-get-transient ~cell) (meta cell)) ~@args)))

(defmacro fetch [f cell & args]
  `(~f ~(with-meta `(phenomena.core/cell-get-transient ~cell) (meta cell)) ~@args))

(defn pod
  [val]
  (phenomena.core/make-cell (Thread/currentThread) val))

(comment

  (extend-type String
    phenomena.core/Editable
    (transient-of [s] (StringBuilder. s)))

  (extend-type StringBuilder
    phenomena.core/Transient
    (value-of [sb] (.toString sb)))

  (def s1
    (let [c (pod "")]
      (dotimes [i 100000]
        (pass .append #^StringBuilder c i))
      @c))

  (def s3
    (let [c (pod "")]
      (dotimes [i 100000]
        (pass .append #^StringBuilder c (fetch .length #^StringBuilder c)))
      @c))
)
