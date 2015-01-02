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
  (let [pol (phenomena.policies.ThreadLockPolicy. (java.util.concurrent.locks.ReentrantLock. true))
        c1 (phenomena.impl.lock-pod/->LockPod pol "" :phenomena.core/nothing {})
        c2 (phenomena.impl.lock-pod/->LockPod pol "" :phenomena.core/nothing {})]
    ;; mutate c1 directly
    (dotimes [i 10]
      (phenomena.core/pass .append #^StringBuilder c1 i))

    (is (= @c1 "0123456789"))

    ;; mutate c2
    ;; (dotimes [i 10]
    ;;   (phenomena.core/pass
    ;;    .append
    ;;    #^StringBuilder c2
    ;;    (phenomena.core/fetch .length #^StringBuilder c2)))

    ;; (is (= @c2 "0123456789"))
    ))
