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
  (is (= 1 1)))

