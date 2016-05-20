(ns imagionary.endpoint.main-test
  (:require [clojure.test :refer :all]
            [imagionary.endpoint.main :as main]))

(def handler
  (main/main-endpoint {}))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))
