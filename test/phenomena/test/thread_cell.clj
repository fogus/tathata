(ns phenomena.test.thread-cell
  (:require phenomena.core
            phenomena.protocols
            [phenomena.policies :as policy])
  (:use [clojure.test]))

(extend-type String
  phenomena.protocols/Editable
  (transient-of [s] (StringBuilder. s)))

(extend-type StringBuilder
  phenomena.protocols/Transient
  (value-of [sb] (.toString sb)))


(deftest test-string-builders
  (let [c1 (phenomena.core/pod "")
        c2 (phenomena.core/pod "")]
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

;; TODO move this into a policy test suite
(deftest test-precept-failures
  (let [c (phenomena.core/pod "" (policy/->ConstructOnly))]
    (is (thrown?
         java.lang.AssertionError
         (phenomena.core/pass .append #^StringBuilder c "should fail")))

    (is (thrown?
         java.lang.AssertionError
         @c))))

;; Transients

(extend-type clojure.lang.IEditableCollection
  phenomena.protocols/Editable
  (transient-of [coll] (.asTransient coll)))

(extend-type clojure.lang.ITransientCollection
  phenomena.protocols/Transient
  (value-of [coll] (.persistent coll)))

(deftest test-transients
  (let [v1 (phenomena.core/pod [])
        v2 (phenomena.core/pod [])]
    ;; mutate
    (dotimes [i 10] (phenomena.core/pass conj! v1 i))

    (is (= @v1 [0 1 2 3 4 5 6 7 8 9]))
    (is (= (count @v1) 10))

    (dotimes [i 10]
      (phenomena.core/pass
       conj!
       v2
       (phenomena.core/fetch count v2)))

    (is (= @v1 [0 1 2 3 4 5 6 7 8 9]))))

