(ns imagionary.endpoint.user
  (:require [clj-brnolib.hiccup :as hiccup]
            [clj-brnolib.jdbc-common :as jdbc-common]
            [clj-brnolib.validation :as validation]
            [clojure.string :as str]
            [compojure.core :refer :all]
            [crypto.password.scrypt :as scrypt]
            [imagionary.endpoint.main-hiccup :as main-hiccup]
            [ring.util.response :as response]
            [taoensso.timbre :as timbre]))

(defn user-endpoint [{{db-spec :spec} :db}]
  (context "" {{user :user} :session}
    (GET "/login" []
      (hiccup/login-page main-hiccup/system-title))

    (POST "/login" [username pwd :as req]
      (try
        (let [user (first (jdbc-common/select db-spec :user {:email username}))]
          (when-not (and user (scrypt/check pwd (:passwd user)))
            (throw (Exception. "Neplatné uživatelské jméno nebo heslo.")))
          (timbre/info "User" username "just logged in.")
          (-> (response/redirect "/" :see-other)
              (assoc-in [:session :user]
                        (timbre/spy
                         (-> user
                             (select-keys [:id :email])
                             (assoc :-roles (->> (str/split (str (:roles user)) #",")
                                                 (map str/trim)
                                                 set)))))))
        (catch Exception e
          (hiccup/login-page main-hiccup/system-title (.getMessage (timbre/spy e))))))

    (GET "/logout" []
      (-> (response/redirect "/" :see-other)
          (assoc :session {})))

    (GET "/passwd" []
      (main-hiccup/imagionary-frame
       user
       (hiccup/passwd-form nil)))

    (POST "/passwd" [old-pwd new-pwd new-pwd2]
      (try
        (let [user (first (jdbc-common/select db-spec :user {:id (:id user)}))]
          (when-not (= new-pwd new-pwd2)
            (throw (Exception. "Zadaná hesla se neshodují.")))
          (when (or (str/blank? new-pwd) (< (count (str/trim new-pwd)) 6))
            (throw (Exception. "Nové heslo je příliš krátké.")))
          (when-not (scrypt/check old-pwd (:passwd user))
            (throw (Exception. "Chybně zadané původní heslo."))))
        (jdbc-common/save! db-spec :user {:id (:id user) :passwd (scrypt/encrypt new-pwd)})
        (main-hiccup/imagionary-frame
         user
         (hiccup/passwd-form {:type :success :msg "Heslo bylo změněno"}))
        (catch Exception e
          (main-hiccup/imagionary-frame
           user
           (hiccup/passwd-form {:type :danger :msg (.getMessage (timbre/spy e))})))))

    (GET "/profile" []
      (timbre/debug "GET /profile")
      (main-hiccup/imagionary-frame
       user
       (hiccup/user-profile-form (first (jdbc-common/select db-spec :user {:id (:id user)})) nil)))

    (POST "/profile" {{:keys [firstname lastname email phone] :as params} :params}
      (timbre/debug "POST /profile")
      (try
        (when (str/blank? firstname)
          (throw (Exception. "Vyplňte své jméno")))
        (when (str/blank? lastname)
          (throw (Exception. "Vyplňte své příjmení")))
        (when-not (validation/valid-email? email)
          (throw (Exception. "Vyplňte správně kontaktní emailovou adresu")))
        (when-not (validation/valid-phone? phone)
          (throw (Exception. "Vyplňte správně kontaktní telefonní číslo")))
        (jdbc-common/save! db-spec :user {:id (:id user) :firstname firstname :lastname lastname :email email :phone phone})
        (main-hiccup/imagionary-frame
         user
         (hiccup/user-profile-form params {:type :success :msg "Změny byly uloženy"}))
        (catch Exception e
          (main-hiccup/imagionary-frame
           user
           (hiccup/user-profile-form params {:type :danger :msg (.getMessage (timbre/spy e))})))))))
