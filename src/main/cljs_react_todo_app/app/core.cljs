(ns cljs-react-todo-app.app.core
  (:require [reagent.dom :as rdom]))

(defn app []
  [:div
   [:section.todoapp
    [:div "Task entry"]
    [:div
     [:div "Task list"]
     [:div "Footer controls"]]]
   [:footer.info
    [:p "Footer info"]]])

(defn render []
  (rdom/render [app] (.getElementById js/document "root")))

(defn ^:export main []
  (render))

(defn ^:dev/after-load reload! []
  (render))
