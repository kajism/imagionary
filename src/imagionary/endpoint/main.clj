(ns imagionary.endpoint.main
  (:require [clj-brnolib.hiccup :as hiccup]
            [clj-brnolib.jdbc-common :as jdbc-common]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [compojure.core :refer :all]
            [imagionary.endpoint.main-hiccup :as main-hiccup]
            [ring.util.response :as response]
            [taoensso.timbre :as timbre]))

(defn- read-csv-lines [file]
  (with-open [in-file (io/reader (:tempfile file) :encoding "cp1250")]
    (doall
     (rest
      (csv/read-csv in-file :separator \;)))))

(defn main-endpoint [{{db-spec :spec} :db}]
  (context "" {{user :user} :session}
    (GET "/kapitoly" []
      (main-hiccup/imagionary-frame
       user
       [:div.container
        [:h3 "Kapitoly"]
        [:ul
         (for [chapter (jdbc-common/select db-spec :chapter {})]
           [:li (:label chapter)])]]))

    (GET "/import" []
      (main-hiccup/imagionary-frame
       user
       [:div.container
        [:h3 "Import slovníku z Excelu"]
        [:form {:method "post" :role "form" :enctype "multipart/form-data"}
         [:div.form-group
          [:label {:for "csv-file"} "CSV soubor"]
          [:input#csv-file.form-control {:name "csv-file" :type "file"}]]
         [:button.btn.btn-success {:type "submit"} "Importovat"]]]))

    (POST "/import" [csv-file]
      (let [lines (read-csv-lines csv-file)]
        (main-hiccup/imagionary-frame
         user
         [:div.container
          [:h3 "Import slovníku z Excelu"]
          [:pre lines]])))

    (GET "/uzivatele" []
      (main-hiccup/imagionary-frame
       user
       [:div.container
        [:h3 "Uživatelé"]
        [:ul
         (for [chapter (jdbc-common/select db-spec :user {})]
           [:li (:email chapter)])]]))))
