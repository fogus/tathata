(ns iter.core-test
  (:require [clojure.test :refer :all]
            [iter.core :as iter]))

(deftest basic-usage-test
  (testing "Basic mapx usage"
    (let [lazy-mapped (iter/mapx inc [1 2 3 4 5])]
      (is (= [2 3 4] (vec (take 3 lazy-mapped))))
      (is (= [2 3 4 5 6] (vec (doall lazy-mapped)))))))

(deftest laziness-test
  (testing "mapx is lazy and side effects only happen on realization"
    (let [side-effects (atom [])
          lazy-with-effects
          (iter/mapx (fn [x]
                       (swap! side-effects conj x)
                       (* x x))
                     (range 10))]

      (is (= [] @side-effects) "No side effects before realization")

      (is (= 0 (first lazy-with-effects)))
      (is (= [0] @side-effects) "Only first element realized")


      (is (= [0 1 4 9 16] (vec (doall (take 5 lazy-with-effects)))))
      (is (= [0 1 2 3 4] @side-effects) "Only realized elements have side effects"))))

(deftest composition-test
  (testing "mapx composes with thread-last macro"
    (let [result (->> (range 10)
                      (iter/mapx inc)
                      (iter/mapx #(* % %))
                      (take 5)
                      doall
                      vec)]
      (is (= [1 4 9 16 25] result)
          "Composed transformations work correctly"))))

(deftest multiple-traversals-test
  (testing "mapx can be traversed multiple times"
    (let [mapped (iter/mapx inc [1 2 3])]
      (is (= [2 3 4] (vec (doall mapped))) "First traversal")
      (is (= [2 3 4] (vec (doall mapped))) "Second traversal produces same result"))))

(deftest empty-collection-test
  (testing "mapx handles empty collections"
    (let [mapped (iter/mapx inc [])]
      (is (= [] (vec (doall mapped)))))))

(deftest nested-mapx-test
  (testing "Multiple mapx operations compose correctly"
    (let [result (->> (range 5)
                      (iter/mapx inc)
                      (iter/mapx #(* % 2))
                      (iter/mapx #(- % 1))
                      doall
                      vec)]
      (is (= [1 3 5 7 9] result)))))

(deftest large-collection-test
  (testing "mapx works efficiently with large collections"
    (let [result (->> (range 10000)
                      (iter/mapx inc)
                      (take 10)
                      doall
                      vec)]
      (is (= [1 2 3 4 5 6 7 8 9 10] result)))))

(deftest filter-integration-test
  (testing "mapx integrates with standard sequence functions"
    (let [result (->> (range 10)
                      (iter/mapx inc)
                      (filter even?)
                      (take 3)
                      doall
                      vec)]
      (is (= [2 4 6] result)))))
