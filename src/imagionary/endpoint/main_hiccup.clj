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
         [:img {:src "/img/logo.jpg" :alt "Imagionary" :height "60"}]]]
       [:div#liskasys-navbar.collapse.navbar-collapse
        [:ul.nav.navbar-nav
         [:li
          [:a {:href "/kapitoly"} "Kapitoly"]]
         (when ((:-roles user) "admin")
           [:li.dropdown
            [:a.dropdown-toggle {:data-toggle "dropdown" :href "#"}
             "Admin" [:span.caret]]
            [:ul.dropdown-menu
             [:li
              [:a {:href "/import"} "Import"]]
             [:li
              [:a {:href "/uzivatele"} "Uživatelé"]]]])]
        [:ul.nav.navbar-nav.navbar-right
         [:li
          [:a {:href "/profile"} (:email user)]]
         [:li
          [:a {:href "/passwd"} "Změna hesla"]]
         [:li
          [:a
           {:href "/logout"} "Odhlásit se"]]]]]]
     body]
    false)))
