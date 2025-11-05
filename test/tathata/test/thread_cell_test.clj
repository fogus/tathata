(ns tathata.test.thread-cell-test
  (:require fogus.kernel.tathata
            fogus.kernel.tathata.protocols
            [fogus.kernel.tathata.policies :as policy])
  (:use [clojure.test]))

(extend-type String
  fogus.kernel.tathata.protocols/ToMutable
  (value->mutable [s] (StringBuilder. s)))

(extend-type StringBuilder
  fogus.kernel.tathata.protocols/ToValue
  (mutable->value [sb] (.toString sb)))


(deftest test-string-builders
  (let [pol (policy/->SingleThreadedRWAccess (Thread/currentThread))
        c1 (fogus.kernel.tathata/pod "" pol)
        c2 (fogus.kernel.tathata/pod "" pol)]
    ;; mutate c1
    (dotimes [i 10]
      (fogus.kernel.tathata/via .append #^StringBuilder c1 i))

    (is (= @c1 "0123456789"))

    ;; mutate c2
    (dotimes [i 10]
      (fogus.kernel.tathata/via
       .append
       #^StringBuilder c2
       (fogus.kernel.tathata/fetch .length #^StringBuilder c2)))

    (is (= @c2 "0123456789"))))

;; TODO move this into a policy test suite
(deftest test-construct-only-failures
  (let [c (fogus.kernel.tathata/pod "foo" (policy/->ConstructOnly))]
    (is (thrown?
         java.lang.AssertionError
         (fogus.kernel.tathata/via .append #^StringBuilder c "should fail")))

    (is (= "foo" @c))))

(deftest test-single-thread-failures
  (let [c (fogus.kernel.tathata/pod "" (policy/->SingleThreadedRWAccess (Thread/currentThread)))
        fut (future-call #(fogus.kernel.tathata/via .append #^StringBuilder c "should fail"))]
    (is (thrown?
         java.util.concurrent.ExecutionException
         @fut))

    (is (= "" @c))))

;; Transients

(extend-type clojure.lang.IEditableCollection
  fogus.kernel.tathata.protocols/ToMutable
  (value->mutable [coll] (.asTransient coll)))

(extend-type clojure.lang.ITransientCollection
  fogus.kernel.tathata.protocols/ToValue
  (mutable->value [coll] (.persistent coll)))

(deftest test-transients
  (let [pol (policy/->SingleThreadedRWAccess (Thread/currentThread))
        v1 (fogus.kernel.tathata/pod [] pol)
        v2 (fogus.kernel.tathata/pod [] pol)]
    ;; mutate
    (dotimes [i 10] (fogus.kernel.tathata/via conj! v1 i))

    (is (= @v1 [0 1 2 3 4 5 6 7 8 9]))
    (is (= (count @v1) 10))

    (dotimes [i 10]
      (fogus.kernel.tathata/via
       conj!
       v2
       (fogus.kernel.tathata/fetch count v2)))

    (is (= @v1 [0 1 2 3 4 5 6 7 8 9]))))

