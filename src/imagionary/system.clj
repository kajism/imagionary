(ns imagionary.system
  (:require [clj-brnolib.middleware :as middleware]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            [duct.component.hikaricp :refer [hikaricp]]
            [duct.component.ragtime :refer [ragtime]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [duct.middleware.route-aliases :refer [wrap-route-aliases]]
            [environ.core :refer [env]]
            [imagionary.endpoint.main :refer [main-endpoint]]
            [imagionary.endpoint.user :refer [user-endpoint]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.session.cookie :as cookie]
            [ring.util.response :as response]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.3rd-party.rotor :refer [rotor-appender]]
            [taoensso.timbre.appenders.core :refer [println-appender]]))

(def base-config
  {:app {:middleware [middleware/wrap-logging
                      [middleware/wrap-auth :api-routes-pattern]
                      [wrap-not-found :not-found]
                      [wrap-defaults :defaults]
                      [wrap-route-aliases :aliases]]
         :api-routes-pattern #"/no-data-api-yet"
         :not-found  (io/resource "imagionary/errors/404.html")
         :defaults   (meta-merge site-defaults (cond-> {:static {:resources "imagionary/public"}
                                                        :security {:anti-forgery false}
                                                        :proxy true}
                                                 (:dev env)
                                                 (assoc :session {:store (cookie/cookie-store {:key "Imagionary--Salt"})})))
         :aliases    {}}
   :ragtime {:resource-path "imagionary/migrations"}})

(defn new-system [config]
  (let [config (meta-merge base-config config)]
    (-> (component/system-map
         :app  (handler-component (:app config))
         :http (jetty-server (:http config))
         :db   (hikaricp (:db config))
         :ragtime (ragtime (:ragtime config))
         :main (endpoint-component main-endpoint)
         :user (endpoint-component user-endpoint))
        (component/system-using
         {:http [:app]
          :app  [:main :user]
          :ragtime [:db]
          :main [:db]
          :user [:db]}))))
