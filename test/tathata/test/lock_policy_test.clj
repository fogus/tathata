(ns tathata.test.lock-policy-test
  (:require tathata.core
            tathata.protocols
            tathata.impl.general-pod
            [tathata.policies :as policy])
  (:use [clojure.test]))

(extend-type String
  tathata.protocols/ToMutable
  (value->mutable [s] (StringBuilder. s)))

(extend-type StringBuilder
  tathata.protocols/ToValue
  (mutable->value [sb] (.toString sb)))

(comment
  (def lock (java.util.concurrent.locks.ReentrantLock. true))
  (def pol (tathata.policies.ThreadLockPolicy. lock))
  (def lp (tathata.impl.general-pod/->GeneralPod pol "" :tathata.core/無 {}))
  
  (tathata.protocols/coordinate
   pol
   #(do
      (println "execing")
      (dotimes [i 10]
        (tathata.core/via .append #^StringBuilder % i))
      42)
   [lp])

  ;; assrtion error
  @lp

  (tathata.core/guarded [lp]
                          @lp)
  ;;=> "01234567890123456789"

  (tathata.core/guarded [lp]
    (dotimes [i 10]
      (tathata.core/via .append #^StringBuilder lp i)))
)


(deftest test-string-builders
  (let [lock (java.util.concurrent.locks.ReentrantLock. true)
        pol (tathata.policies.ThreadLockPolicy. lock)
        c1 (tathata.impl.general-pod/->GeneralPod pol "" :tathata.core/無 {})
        c2 (tathata.impl.general-pod/->GeneralPod pol "" :tathata.core/無 {})]
    ;; mutate c1 directly
    (.lock lock)
    (try
      (dotimes [i 10]
        (tathata.core/via .append #^StringBuilder c1 i))

      (is (= @c1 "0123456789"))
      (finally (.unlock lock)))

    (is (thrown?
         java.lang.AssertionError
         (tathata.core/via .append #^StringBuilder c1 "should fail")))

    (is (thrown? java.lang.AssertionError (tathata.protocols/render c1)))
    
    ;; mutate c2
    (tathata.core/guarded [c2]
     (dotimes [i 10]
       (tathata.core/via
        .append
        #^StringBuilder c2
        (tathata.core/fetch .length #^StringBuilder c2)))

     (is (= @c2 "0123456789")))))
