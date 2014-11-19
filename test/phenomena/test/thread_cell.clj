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
  (let [c1 (phenomena.impl.thread-cell/thread-pod "")
        c2 (phenomena.impl.thread-cell/thread-pod "")]
    ;; mutate c1
    (dotimes [i 10]
      (phenomena.core/pass .append #^StringBuilder c1 i))

    (is (= @c1 "0123456789"))

    ;; mutate c2
    (dotimes [i 10]
      (phenomena.core/pass
       .append
       #^StringBuilder c2
       (phenomena.core/fetch .length #^StringBuilder c2)))

    (is (= @c2 "0123456789"))))

