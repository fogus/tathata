(ns tathata.test.lock-policy-test
  (:require fogus.tathata
            fogus.tathata.protocols
            fogus.tathata.impl.general-pod
            [fogus.tathata.policies :as policy])
  (:use [clojure.test]))

(extend-type String
  fogus.tathata.protocols/ToMutable
  (value->mutable [s] (StringBuilder. s)))

(extend-type StringBuilder
  fogus.tathata.protocols/ToValue
  (mutable->value [sb] (.toString sb)))

(comment
  (def lock (java.util.concurrent.locks.ReentrantLock. true))
  (def pol (fogus.tathata.policies.ThreadLockPolicy. lock))
  (def lp (fogus.tathata.impl.general-pod/->GeneralPod pol "" :tathata.core/無 {}))
  
  (fogus.tathata.protocols/coordinate
   pol
   #(do
      (println "execing")
      (dotimes [i 10]
        (fogus.tathata/via .append #^StringBuilder % i))
      42)
   [lp])

  ;; assrtion error
  @lp

  (fogus.tathata/guarded [lp]
                          @lp)
  ;;=> "01234567890123456789"

  (tathata.core/guarded [lp]
    (dotimes [i 10]
      (fogus.tathata/via .append #^StringBuilder lp i)))
)


(deftest test-string-builders
  (let [lock (java.util.concurrent.locks.ReentrantLock. true)
        pol (fogus.tathata.policies.ThreadLockPolicy. lock)
        c1 (fogus.tathata.impl.general-pod/->GeneralPod pol "" :tathata.core/無 {})
        c2 (fogus.tathata.impl.general-pod/->GeneralPod pol "" :tathata.core/無 {})]
    ;; mutate c1 directly
    (.lock lock)
    (try
      (dotimes [i 10]
        (fogus.tathata/via .append #^StringBuilder c1 i))

      (is (= @c1 "0123456789"))
      (finally (.unlock lock)))

    (is (thrown?
         java.lang.AssertionError
         (fogus.tathata/via .append #^StringBuilder c1 "should fail")))

    (is (thrown? java.lang.AssertionError (fogus.tathata.protocols/render c1)))
    
    ;; mutate c2
    (fogus.tathata/guarded [c2]
     (dotimes [i 10]
       (fogus.tathata/via
        .append
        #^StringBuilder c2
        (fogus.tathata/fetch .length #^StringBuilder c2)))

     (is (= @c2 "0123456789")))))
