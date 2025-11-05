(ns tathata.test.pods-test
  (:require [clojure.test :refer :all]
            [fogus.kernel.tathata :as tathata]
            [fogus.kernel.tathata.protocols :as proto]
            [fogus.kernel.tathata.policies :as policies])
  (:import [java.lang StringBuilder]))

(extend-type StringBuilder
  proto/ToValue
  (mutable->value [sb] (.toString sb))
  (mutable->value [sb sentry] (.toString sb)))

(extend-type String
  proto/ToMutable
  (value->mutable [s] (StringBuilder. s))
  (value->mutable [s policy] (StringBuilder. s)))

(deftest single-threaded-append-test
  (testing "StringBuilder in a single-threaded pod"
    (let [policy (policies/->SingleThreadedRWAccess (Thread/currentThread))
          sb-pod (tathata/pod "" policy)]
      
      (tathata/via .append sb-pod "Hello")
      (tathata/via .append sb-pod " ")
      (tathata/via .append sb-pod "World")
      
      (is (= "Hello World" @sb-pod)))))

(deftest multiple-operations-test
  (testing "Various StringBuilder operations through pod"
    (let [policy (policies/->SingleThreadedRWAccess (Thread/currentThread))
          sb-pod (tathata/pod "Start" policy)]
      (tathata/via .append sb-pod " -> ")
      (tathata/via .append sb-pod "Middle")
      (tathata/via .append sb-pod " -> ")
      (tathata/via .append sb-pod "End")
      
      (is (= "Start -> Middle -> End" @sb-pod)))))

(deftest render-clears-ephemeron-test
  (testing "Rendering converts ephemeron to value"
    (let [policy (policies/->SingleThreadedRWAccess (Thread/currentThread))
          sb-pod (tathata/pod "Initial" policy)]     
      (tathata/via .append sb-pod " Text")
      (is (= "Initial Text" @sb-pod))
      
      (tathata/via .append sb-pod " More")
      (is (= "Initial Text More" @sb-pod)))))

(deftest construct-only-policy-test
  (testing "ConstructOnly policy prevents access after construction"
    (let [policy (policies/->ConstructOnly)
          sb-pod (tathata/pod "Fixed Text" policy)]
      (is (= "Fixed Text" @sb-pod))
      (is (thrown? AssertionError (tathata/via .append sb-pod " More"))))))

(deftest fetch-vs-via-test
  (testing "fetch reads without mutation, via mutates"
    (let [policy (policies/->SingleThreadedRWAccess (Thread/currentThread))
          sb-pod (tathata/pod "Test" policy)]
      
      (is (= 4 (tathata/fetch .length sb-pod)))
      
      (tathata/via .append sb-pod "ing")
      (is (= 7 (tathata/fetch .length sb-pod)))
      
      (is (= "Testing" @sb-pod)))))

(deftest empty-string-test
  (testing "StringBuilder pod with empty string"
    (let [policy (policies/->SingleThreadedRWAccess (Thread/currentThread))
          sb-pod (tathata/pod "" policy)]
      (is (= "" @sb-pod) "Empty pod renders as empty string")
      
      (tathata/via .append sb-pod "Now has content")
      (is (= "Now has content" @sb-pod)))))

(deftest large-string-building-test
  (testing "Building a large string through pod"
    (let [policy (policies/->SingleThreadedRWAccess (Thread/currentThread))
          sb-pod (tathata/pod "" policy)]
      (dotimes [i 100]
        (tathata/via .append sb-pod i)
        (when (< i 99)
          (tathata/via .append sb-pod ",")))
      
      (let [result @sb-pod]
        (is (string? result))
        (is (.startsWith result "0,1,2,"))
        (is (.endsWith result ",98,99"))))))

(deftest chaining-operations-test
  (testing "StringBuilder operations can be chained"
    (let [policy (policies/->SingleThreadedRWAccess (Thread/currentThread))
          sb-pod (tathata/pod "Base" policy)]
      (tathata/via .append sb-pod " Layer1")
      (tathata/via .append sb-pod " Layer2")
      (tathata/via .append sb-pod " Layer3")
      
      (is (= "Base Layer1 Layer2 Layer3" @sb-pod)))))

(deftest delete-operations-test
  (testing "StringBuilder delete operations through pod"
    (let [policy (policies/->SingleThreadedRWAccess (Thread/currentThread))
          sb-pod (tathata/pod "Hello World" policy)]
      (tathata/via .delete sb-pod 6 11)
      (is (= "Hello " @sb-pod))
      
      (tathata/via .append sb-pod "Clojure")
      (is (= "Hello Clojure" @sb-pod)))))
