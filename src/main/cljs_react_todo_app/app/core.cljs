(ns cljs-react-todo-app.app.core
  (:require [reagent.dom :as rdom]))

(defn todo-input []
  [:input {:class "new-todo"
           :placeholder "Todo input"
           :type "text"}])

(defn task-entry []
  [:header.header
   [:h1 "todos"]
   [todo-input]])

(defn task-list []
  [:section.main
   [:div "Todo list"]])

(defn footer-controls []
  [:footer.footer
   [:div "Footer controls"]])

(defn app []
  [:div
   [:section.todoapp
    [task-entry]
    [:div
     [task-list]
     [footer-controls]]]
   [:footer.info
    [:p "Footer info"]]])

(defn render []
  (rdom/render [app] (.getElementById js/document "root")))

(defn ^:export main []
  (render))

(defn ^:dev/after-load reload! []
  (render))
