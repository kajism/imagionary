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
      (csv/read-csv in-file)))))

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
      (let [lines (->> (read-csv-lines csv-file)
                       (filter (fn [[chapter-label]]
                                 (and (not (str/blank? chapter-label))
                                      (not= "název kapitoly" (str/lower-case chapter-label))))))
            chapter-label->id (atom (into {} (map (juxt :label :id)
                                                  (jdbc-common/select db-spec :chapter {}))))
            tag-label->id (atom (into {} (map (juxt :label :id)
                                              (jdbc-common/select db-spec :tag {}))))
            word-chapter->id nil  #_(atom (into {} (map (juxt :label :id)
                                                 (jdbc-common/select db-spec :tag {}))))]
        (main-hiccup/imagionary-frame
         user
         [:div.container
          [:h3 "Import slovníku z Excelu"]
          [:ul
           (for [[chapter-label word img-filename copyright explanation tags notes _ syllables :as line] (timbre/spy lines)
                 :when (and (timbre/spy line) (timbre/spy chapter-label) (timbre/spy img-filename))
                 :let [tags-id (->> (str/split tags #"\s*,\s*")
                                    (map #(or (get @tag-label->id %)
                                              (let [new-id (jdbc-common/insert! db-spec :tag {:label %})]
                                                (swap! tag-label->id assoc % new-id)
                                                new-id)))
                                    set)
                       chapter-id (or (get @chapter-label->id chapter-label)
                                      (let [new-id (jdbc-common/insert! db-spec :chapter {:label chapter-label})]
                                        (swap! chapter-label->id assoc chapter-label new-id)
                                        new-id))
                       item (cond-> {:chapter-id chapter-id
                                     :word word
                                     :img-filename img-filename
                                     :copyright copyright
                                     :explanation explanation
                                     :notes notes}
                              (not (str/blank? syllables))
                              (assoc :syllables (Byte. syllables)))]]
             [:li (pr-str item)])]])))

    (GET "/uzivatele" []
      (main-hiccup/imagionary-frame
       user
       [:div.container
        [:h3 "Uživatelé"]
        [:ul
         (for [chapter (jdbc-common/select db-spec :user {})]
           [:li (:email chapter)])]]))))
