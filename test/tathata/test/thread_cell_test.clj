(ns tathata.test.thread-cell-test
  (:require tathata.core
            tathata.protocols
            [tathata.policies :as policy])
  (:use [clojure.test]))

(extend-type String
  tathata.protocols/ToMutable
  (value->mutable [s] (StringBuilder. s)))

(extend-type StringBuilder
  tathata.protocols/ToValue
  (mutable->value [sb] (.toString sb)))


(deftest test-string-builders
  (let [pol (policy/->SingleThreadedRWAccess (Thread/currentThread))
        c1 (tathata.core/pod "" pol)
        c2 (tathata.core/pod "" pol)]
    ;; mutate c1
    (dotimes [i 10]
      (tathata.core/via .append #^StringBuilder c1 i))

    (is (= @c1 "0123456789"))

    ;; mutate c2
    (dotimes [i 10]
      (tathata.core/via
       .append
       #^StringBuilder c2
       (tathata.core/fetch .length #^StringBuilder c2)))

    (is (= @c2 "0123456789"))))

;; TODO move this into a policy test suite
(deftest test-construct-only-failures
  (let [c (tathata.core/pod "foo" (policy/->ConstructOnly))]
    (is (thrown?
         java.lang.AssertionError
         (tathata.core/via .append #^StringBuilder c "should fail")))

    (is (= "foo" @c))))

(deftest test-single-thread-failures
  (let [c (tathata.core/pod "" (policy/->SingleThreadedRWAccess (Thread/currentThread)))
        fut (future-call #(tathata.core/via .append #^StringBuilder c "should fail"))]
    (is (thrown?
         java.util.concurrent.ExecutionException
         @fut))

    (is (= "" @c))))

;; Transients

(extend-type clojure.lang.IEditableCollection
  tathata.protocols/ToMutable
  (value->mutable [coll] (.asTransient coll)))

(extend-type clojure.lang.ITransientCollection
  tathata.protocols/ToValue
  (mutable->value [coll] (.persistent coll)))

(deftest test-transients
  (let [pol (policy/->SingleThreadedRWAccess (Thread/currentThread))
        v1 (tathata.core/pod [] pol)
        v2 (tathata.core/pod [] pol)]
    ;; mutate
    (dotimes [i 10] (tathata.core/via conj! v1 i))

    (is (= @v1 [0 1 2 3 4 5 6 7 8 9]))
    (is (= (count @v1) 10))

    (dotimes [i 10]
      (tathata.core/via
       conj!
       v2
       (tathata.core/fetch count v2)))

    (is (= @v1 [0 1 2 3 4 5 6 7 8 9]))))


(comment

  (def p (tathata.core/pod ""))
  @p

  (instance? tathata.protocols.Aggregable (.policy p))

)
