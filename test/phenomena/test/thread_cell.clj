(ns phenomena.test.thread-cell
  (:require phenomena.core
            phenomena.impl.thread-cell)
  (:use [clojure.test]))

(extend-type String
  phenomena.core/Editable
  (transient-of [s] (StringBuilder. s)))

(extend-type StringBuilder
  phenomena.core/Transient
  (value-of [sb] (.toString sb)))

(deftest test-string-builders
  (let [c (phenomena.impl.thread-cell/thread-pod "")]
    (dotimes [i 10]
      (phenomena.impl.thread-cell/pass .append #^StringBuilder c i))

    (is (= @c "0123456789"))))

