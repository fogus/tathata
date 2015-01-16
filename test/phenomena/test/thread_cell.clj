(ns phenomena.test.thread-cell
  (:require phenomena.core
            phenomena.protocols
            [phenomena.policies :as policy])
  (:use [clojure.test]))

(extend-type String
  phenomena.protocols/ToTransient
  (value->transient [s] (StringBuilder. s)))

(extend-type StringBuilder
  phenomena.protocols/ToValue
  (transient->value [sb] (.toString sb)))


(deftest test-string-builders
  (let [pol (policy/->SingleThreadedRWAccess (Thread/currentThread))
        c1 (phenomena.core/pod "" pol)
        c2 (phenomena.core/pod "" pol)]
    ;; mutate c1
    (dotimes [i 10]
      (phenomena.core/via .append #^StringBuilder c1 i))

    (is (= @c1 "0123456789"))

    ;; mutate c2
    (dotimes [i 10]
      (phenomena.core/via
       .append
       #^StringBuilder c2
       (phenomena.core/fetch .length #^StringBuilder c2)))

    (is (= @c2 "0123456789"))))

;; TODO move this into a policy test suite
(deftest test-construct-only-failures
  (let [c (phenomena.core/pod "foo" (policy/->ConstructOnly))]
    (is (thrown?
         java.lang.AssertionError
         (phenomena.core/via .append #^StringBuilder c "should fail")))

    (is (= "foo" @c))))

(deftest test-single-thread-failures
  (let [c (phenomena.core/pod "" (policy/->SingleThreadedRWAccess (Thread/currentThread)))
        fut (future-call #(phenomena.core/via .append #^StringBuilder c "should fail"))]
    (is (thrown?
         java.util.concurrent.ExecutionException
         @fut))

    (is (= "" @c))))

;; Transients

(extend-type clojure.lang.IEditableCollection
  phenomena.protocols/ToTransient
  (value->transient [coll] (.asTransient coll)))

(extend-type clojure.lang.ITransientCollection
  phenomena.protocols/ToValue
  (transient->value [coll] (.persistent coll)))

(deftest test-transients
  (let [pol (policy/->SingleThreadedRWAccess (Thread/currentThread))
        v1 (phenomena.core/pod [] pol)
        v2 (phenomena.core/pod [] pol)]
    ;; mutate
    (dotimes [i 10] (phenomena.core/via conj! v1 i))

    (is (= @v1 [0 1 2 3 4 5 6 7 8 9]))
    (is (= (count @v1) 10))

    (dotimes [i 10]
      (phenomena.core/via
       conj!
       v2
       (phenomena.core/fetch count v2)))

    (is (= @v1 [0 1 2 3 4 5 6 7 8 9]))))


(comment

  (def p (phenomena.core/pod ""))
  @p

  (instance? phenomena.protocols.Aggregable (.policy p))

)