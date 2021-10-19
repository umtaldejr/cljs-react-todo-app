(ns cljs-react-todo-app.app.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

;; ----- App State -----

(def initial-todos {1 {:id 1, :title "Do laundry", :done false}
                    3 {:id 3, :title "Buy groceries", :done false}
                    2 {:id 2, :title "Wash dishes", :done true}})

(def initial-todos-sorted (into (sorted-map) initial-todos))

(defonce todos (r/atom initial-todos-sorted))

;; ----- Views -----

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

;; ----- Render -----

(defn render []
  (rdom/render [app] (.getElementById js/document "root")))

(defn ^:export main []
  (render))

(defn ^:dev/after-load reload! []
  (render))
