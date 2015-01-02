(ns phenomena.test.lock-cell
  (:require phenomena.core
            phenomena.protocols
            phenomena.impl.lock-pod
            [phenomena.policies :as policy])
  (:use [clojure.test]))

(extend-type String
  phenomena.protocols/Editable
  (transient-of [s] (StringBuilder. s)))

(extend-type StringBuilder
  phenomena.protocols/Transient
  (value-of [sb] (.toString sb)))

(comment
  (phenomena.protocols/coordinate
   pol
   #(do
      (println "execing")
      (dotimes [i 10]
        (phenomena.core/pass .append #^StringBuilder lp i))
      42))

  @lp

  (phenomena.core/guarded [lp]
    (dotimes [i 10]
      (phenomena.core/pass .append #^StringBuilder lp i)))
)


(deftest test-string-builders
  (let [lock (java.util.concurrent.locks.ReentrantLock. true)
        pol (phenomena.policies.ThreadLockPolicy. lock)
        c1 (phenomena.impl.lock-pod/->LockPod pol "" :phenomena.core/nothing {})
        c2 (phenomena.impl.lock-pod/->LockPod pol "" :phenomena.core/nothing {})]
    ;; mutate c1 directly
    (.lock lock)
    (try
      (dotimes [i 10]
        (phenomena.core/pass .append #^StringBuilder c1 i))

      (is (= @c1 "0123456789"))
      (finally (.unlock lock)))

    (is (thrown?
         java.lang.AssertionError
         (phenomena.core/pass .append #^StringBuilder c1 "should fail")))

    (is (thrown? java.lang.AssertionError (phenomena.protocols/pod-render c1)))
    
    ;; mutate c2
    (phenomena.core/guarded [c2]
     (dotimes [i 10]
       (phenomena.core/pass
        .append
        #^StringBuilder c2
        (phenomena.core/fetch .length #^StringBuilder c2)))

     (is (= @c2 "0123456789")))))