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

(defn- import-csv-file-to-db [db-spec csv-file]
  (let [lines (->> (read-csv-lines csv-file)
                   (filter (fn [[chapter-label]]
                             (and (not (str/blank? chapter-label))
                                  (not= "název kapitoly" (str/lower-case chapter-label))))))
        chapter-label->id (atom (into {} (map (juxt :label :id)
                                              (jdbc-common/select db-spec :chapter {}))))
        tag-label->id (atom (into {} (map (juxt :label :id)
                                          (jdbc-common/select db-spec :tag {}))))
        chapter-img->id (into {} (map (juxt (juxt :chapter-id :img-filename)
                                            :id)
                                      (jdbc-common/select db-spec :imagionary {})))]
    (doseq [[chapter-label word img-filename copyright explanation tags notes _ syllables :as line] lines
            :when (and line chapter-label img-filename)
            :let [tag-ids (->> (str/split tags #"\s*,\s*")
                               (map #(or (get @tag-label->id %)
                                         (let [new-id (jdbc-common/insert! db-spec :tag {:label %})]
                                           (swap! tag-label->id assoc % new-id)
                                           new-id)))
                               set)
                  chapter-id (or (get @chapter-label->id chapter-label)
                                 (let [new-id (jdbc-common/insert! db-spec :chapter {:label chapter-label})]
                                   (swap! chapter-label->id assoc chapter-label new-id)
                                   new-id))
                  id (get chapter-img->id [chapter-id img-filename])
                  row (cond-> {:chapter-id chapter-id
                               :word word
                               :img-filename img-filename
                               :copyright copyright
                               :explanation explanation
                               :notes notes}
                        (not (str/blank? syllables))
                        (assoc :syllables (Byte. syllables))
                        id
                        (assoc :id id))
                  id (if id
                       (jdbc-common/update! db-spec :imagionary row)
                       (jdbc-common/insert! db-spec :imagionary row))]]
      (jdbc-common/delete! db-spec :imagionary-tag {:imagionary-id id})
      (doseq [tag-id tag-ids]
        (jdbc-common/insert! db-spec :imagionary-tag {:imagionary-id id
                                                      :tag-id tag-id})))))

(defn- imagionary-item [row]
  [:div
   [:div [:b (:word row)]]
   [:div (:explanation row)]
   [:img {:src (str "/img/" (:img-filename row))}]
   [:div (:copyright row)]])

(defn main-endpoint [{{db-spec :spec} :db}]
  (context "" {{user :user} :session}
    (GET "/kapitoly" []
      (let [chapters (jdbc-common/select db-spec :chapter {})]
        (main-hiccup/imagionary-frame
         user
         [:div.container
          [:h3 "Kapitoly"]
          [:ul
           (for [row chapters]
             [:li [:a {:href (str "/kapitola/" (:id row))} (:label row)]])]])))

    (GET "/kapitola/:id" [id]
      (let [chapter (first (jdbc-common/select db-spec :chapter {:id id}))
            imgs (jdbc-common/select db-spec :imagionary {:chapter-id id})]
        (main-hiccup/imagionary-frame
         user
         [:div.container
          [:h3 "Kapitola: " (:label chapter)]
          [:ul
           (for [row imgs]
             [:li (imagionary-item row)])]])))

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
      (import-csv-file-to-db db-spec csv-file)
      (main-hiccup/imagionary-frame
       user
       [:div.container
        [:h3 "Import slovníku z Excelu"]
        [:div "Hotovo"]]))

    (GET "/uzivatele" []
      (let [users (jdbc-common/select db-spec :user {})]
        (main-hiccup/imagionary-frame
         user
         [:div.container
          [:h3 "Uživatelé"]
          [:ul
           (for [row users]
             [:li (:email row)])]])))))
