(ns imagionary.endpoint.user
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]))

(defn user-endpoint [{{db :spec} :db}]
  (context "/user" []
    (GET "/" []
      "")))
