(ns imagionary.endpoint.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]))

(defn example-endpoint [config]
  (context "/example" []
    (GET "/" []
      (io/resource "imagionary/endpoint/example/example.html"))))
