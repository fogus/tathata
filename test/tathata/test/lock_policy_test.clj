(ns tathata.test.lock-policy-test
  (:require fogus.kernel.tathata
            fogus.kernel.tathata.protocols
            fogus.kernel.tathata.impl.general-pod
            [fogus.kernel.tathata.policies :as policy])
  (:use [clojure.test]))

(extend-type String
  fogus.kernel.tathata.protocols/ToMutable
  (value->mutable [s] (StringBuilder. s)))

(extend-type StringBuilder
  fogus.kernel.tathata.protocols/ToValue
  (mutable->value [sb] (.toString sb)))

(comment
  (def lock (java.util.concurrent.locks.ReentrantLock. true))
  (def pol (fogus.kernel.tathata.policies.ThreadLockPolicy. lock))
  (def lp (fogus.kernel.tathata.impl.general-pod/->GeneralPod pol "" :tathata.core/無 {}))
  
  (fogus.kernel.tathata.protocols/coordinate
   pol
   #(do
      (println "execing")
      (dotimes [i 10]
        (fogus.kernel.tathata/via .append #^StringBuilder % i))
      42)
   [lp])

  ;; assrtion error
  @lp

  (fogus.kernel.tathata/guarded [lp]
                          @lp)
  ;;=> "01234567890123456789"

  (tathata.core/guarded [lp]
    (dotimes [i 10]
      (fogus.kernel.tathata/via .append #^StringBuilder lp i)))
)


(deftest test-string-builders
  (let [lock (java.util.concurrent.locks.ReentrantLock. true)
        pol (fogus.kernel.tathata.policies.ThreadLockPolicy. lock)
        c1 (fogus.kernel.tathata.impl.general-pod/->GeneralPod pol "" :tathata.core/無 {})
        c2 (fogus.kernel.tathata.impl.general-pod/->GeneralPod pol "" :tathata.core/無 {})]
    ;; mutate c1 directly
    (.lock lock)
    (try
      (dotimes [i 10]
        (fogus.kernel.tathata/via .append #^StringBuilder c1 i))

      (is (= @c1 "0123456789"))
      (finally (.unlock lock)))

    (is (thrown?
         java.lang.AssertionError
         (fogus.kernel.tathata/via .append #^StringBuilder c1 "should fail")))

    (is (thrown? java.lang.AssertionError (fogus.kernel.tathata.protocols/render c1)))
    
    ;; mutate c2
    (fogus.kernel.tathata/guarded [c2]
     (dotimes [i 10]
       (fogus.kernel.tathata/via
        .append
        #^StringBuilder c2
        (fogus.kernel.tathata/fetch .length #^StringBuilder c2)))

     (is (= @c2 "0123456789")))))
