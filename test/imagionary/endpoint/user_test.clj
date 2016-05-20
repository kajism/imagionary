(ns imagionary.endpoint.user-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [imagionary.endpoint.user :as user]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]))

(def handler
  (user/user-endpoint {}))

(deftest smoke-test
  (testing "user page exists"
    (-> (session handler)
        (visit "/user")
        (has (status? 200) "page exists"))))
