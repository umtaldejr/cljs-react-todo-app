(ns cljs-react-todo-app.app.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [clojure.string :as str]
            [cljs.pprint :as pp]))

;; ----- App State -----

(defonce todos (r/atom (sorted-map)))

(defonce counter (r/atom 0))

;; ----- Watch -----

(add-watch todos :todos
           (fn [key _atom _old-state new-state]
             (println "---" key "atom changed ---")
             (pp/pprint new-state)))

;; ----- Utils -----

(defn add-todo [text]
  (let [id (swap! counter inc)
        new-todo {:id id :title text :done false}]
    (swap! todos assoc id new-todo)))

(defn delete-todo [id]
  (swap! todos dissoc id))

(defn toggle-done [id]
  (swap! todos update-in [id :done] not))

;; ----- Seed -----

(defonce init (do
                (add-todo "Do laundry")
                (add-todo "Wash dishes")
                (add-todo "Buy groceries")))

;; ----- Views -----

(defn todo-input [{:keys [on-save ]}]
  (let [input-text (r/atom "")
        update-text #(reset! input-text %)
        stop #(reset! input-text "")
        save #(let [trimmed-text (-> @input-text str str/trim)]
                (if-not (empty? trimmed-text) (on-save trimmed-text))
                (stop))
        key-pressed #(case %
                       "Enter" (save)
                       "Esc" (stop)
                       "Escape" (stop)
                       nil)]
    (fn [{:keys [class placeholder]}]
      [:input {:class class
               :placeholder placeholder
               :type "text"
               :value @input-text
               :on-blur save
               :on-change #(update-text (.. % -target -value))
               :on-key-down #(key-pressed (.. % -key))}])))

(defn task-entry []
  [:header.header
   [:h1 "todos"]
   [todo-input {:class "new-todo"
                :placeholder "What needs to be done?"
                :on-save add-todo}]])

(defn todo-item [{:keys [id title done]}]
  [:li {:class (when done "completed ")}
   [:div.view
    [:input {:class "toggle"
             :type "checkbox"
             :checked done
             :on-change #(toggle-done id)}]
    [:label title]
    [:button.destroy {:on-click #(delete-todo id)}]]])

(defn task-list []
  (let [items (vals @todos)]
    [:section.main
     [:ul.todo-list
      (for [todo items]
        ^{:key (:id todo)} [todo-item todo])]]))

(defn footer-controls []
  [:footer.footer
   [:div "Footer controls"]])

(defn app []
  [:div
   [:section.todoapp
    [task-entry]
    (when (seq @todos)
      [:div
       [task-list]
       [footer-controls]])]
   [:footer.info
    [:p "Footer info"]]])

;; ----- Render -----

(defn render []
  (rdom/render [app] (.getElementById js/document "root")))

(defn ^:export main []
  (render))

(defn ^:dev/after-load reload! []
  (render))
