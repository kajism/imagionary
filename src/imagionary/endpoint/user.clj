(ns imagionary.endpoint.user
  (:require [clj-brnolib.hiccup :as hiccup]
            [clj-brnolib.jdbc-common :as jdbc-common]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [compojure.core :refer :all]
            [crypto.password.scrypt :as scrypt]
            [imagionary.endpoint.main-hiccup :as main-hiccup]
            [ring.util.response :as response]
            [taoensso.timbre :as timbre]))

(defn user-endpoint [{{db-spec :spec} :db}]
  (context "" []
    (GET "/login" []
      (timbre/debug "GET /login")
      (hiccup/login-page main-hiccup/system-title))

    (POST "/login" [username pwd :as req]
      (timbre/debug "POST /login")
      (try
        (let [user (first (jdbc-common/select db-spec :user {:email username}))]
          (when-not (and user (scrypt/check pwd (:passwd user)))
            (throw (Exception. "Neplatné uživatelské jméno nebo heslo.")))
          (timbre/info "User" username "just logged in.")
          (-> (response/redirect "/" :see-other)
              (assoc-in [:session :user]
                        (-> user
                            (select-keys [:id :email])
                            (assoc :-roles (->> (str/split (str (:roles user)) #",")
                                                (map str/trim)
                                                set))))))
        (catch Exception e
          (hiccup/login-page main-hiccup/system-title (.getMessage (timbre/spy e))))))

    (GET "/logout" []
      (timbre/debug "GET /logout")
      (-> (response/redirect "/" :see-other)
          (assoc :session {})))))
