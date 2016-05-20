(ns imagionary.endpoint.main
  (:require [compojure.core :refer :all]))

(defn main-endpoint [config]
  (routes
   (GET "/" [] "Hello World")))
