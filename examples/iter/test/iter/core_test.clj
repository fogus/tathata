(ns iter.core-test
  (:require [clojure.test :refer :all]
            [iter.core :as iter]))

(deftest a-test
  (testing "mapx test"
    (is (= [2 3 4] (iter/mapx inc [1 2 3])))))
