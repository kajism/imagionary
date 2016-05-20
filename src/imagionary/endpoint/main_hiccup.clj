(ns imagionary.endpoint.main-hiccup
  (:require [clj-brnolib.hiccup :as hiccup]
            [clojure.pprint :refer [pprint]]
            [taoensso.timbre :as timbre]))

(def system-title "Obrázkový slovník")

(defn imagionary-frame [user body]
  (hiccup/hiccup-response
   (hiccup/hiccup-frame
    system-title
    [:div
     [:nav.navbar.navbar-default
      [:div.container-fluid
       [:div.navbar-header
        [:button.navbar-toggle {:type "button" :data-toggle "collapse" :data-target "#liskasys-navbar"}
         [:span.icon-bar]
         [:span.icon-bar]
         [:span.icon-bar]]
        [:a {:href "#"}
         [:img {:src "/img/logo_background.jpg" :alt "LiškaSys" :height "60"}]]]
       [:div#liskasys-navbar.collapse.navbar-collapse
        [:ul.nav.navbar-nav
         (when (pos? (:-children-count user))
           [:li
            [:a {:href "/"} "Omluvenky"]])
         (when (or ((:-roles user) "admin")
                   ((:-roles user) "obedy"))
           [:li
            [:a {:href "/obedy"} "Obědy"]])]
        [:ul.nav.navbar-nav.navbar-right
         [:li
          [:a {:href "/profile"} (:-fullname user)]]
         (when ((:-roles user) "admin")
           [:li
            [:a {:target "admin" :href "/admin.app"} "Admin"]])
         [:li
          [:a {:href "/passwd"} "Změna hesla"]]
         [:li
          [:a
           {:href "/logout"} "Odhlásit se"]]]]]]
     body]
    false)))
