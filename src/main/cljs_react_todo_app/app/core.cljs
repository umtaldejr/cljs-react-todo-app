(ns cljs-react-todo-app.app.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [clojure.string :as str]
            [cljs.pprint :as pp]
            [cljs.reader :as reader]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as gevents]
            [goog.history.EventType :as EventType])
  (:import goog.history.Html5History))

;; ----- App State -----

(defonce db (r/atom {:todos (sorted-map)
                     :showing :all}))

;; ----- Local Storage -----

(def local-storage-key "todo-app")

(defn todos->local-storage []
  (.setItem js/localStorage local-storage-key (str (:todos @db))))

(defn local-storage->todos []
  (let [edn-map-todos (.getItem js/localStorage local-storage-key)
        unsorted-todos (some->> edn-map-todos reader/read-string)
        sorted-todos (into (sorted-map) unsorted-todos)]
    (swap! db assoc :todos sorted-todos)))

;; ----- Watch -----

(add-watch db :db
           (fn [key _atom old-state new-state]
             (when (not= (:todos new-state) (:todos old-state))
               (todos->local-storage)
               (println "---" key "atom changed ---")
               (pp/pprint new-state))))

;; ----- Utils -----

(defn set-showing [kw]
  (swap! db assoc :showing kw))

(defn allocate-next-id [todos]
  ((fnil inc 0) (last (keys todos))))

(defn add-todo [text]
  (let [id (allocate-next-id (:todos @db))
        new-todo {:id id :title text :done false}]
    (swap! db assoc-in [:todos id] new-todo)))

(defn delete-todo [id]
  (swap! db update-in [:todos] dissoc id))

(defn save-todo [id title]
  (swap! db assoc-in [:todos id :title] title))

(defn toggle-done [id]
  (swap! db update-in [:todos id :done] not))

(defn complete-all-toggle []
  (let [todos (:todos @db)
        b (not-every? :done (vals todos))
        g (fn [m id]
            (assoc-in m [id :done] b))
        ids (keys todos)
        updated-todos (reduce g todos ids)]
    (swap! db assoc :todos updated-todos)))

(defn clear-completed []
  (let [todos (:todos @db)
        done-ids (->> (vals todos)
                      (filter :done)
                      (map :id))
        updated-todos (reduce dissoc todos done-ids)]
    (swap! db assoc :todos updated-todos)))

;; ----- Seed -----

#_(defonce init (do
                  (add-todo "Do laundry")
                  (add-todo "Wash dishes")
                  (add-todo "Buy groceries")))

;; ----- Routing -----

(defn hook-browser-navigation! []
  (doto (Html5History.)
    (gevents/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.. ^js event -token))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  (defroute "/" [] (set-showing :all))
  (defroute "/:filter" [filter] (set-showing (keyword filter)))
  (hook-browser-navigation!))

;; ----- Views -----

(defn todo-input [{:keys [title on-save on-stop]}]
  (let [input-text (r/atom title)
        update-text #(reset! input-text %)
        stop #(do (reset! input-text "")
                  (when on-stop (on-stop)))
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
               :auto-focus true
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

(defn todo-item [_props-map]
  (let [editing (r/atom false)]
    (fn [{:keys [id title done]}]
      [:li {:class (str (when done "completed ")
                        (when @editing "editing"))}
       [:div.view
        [:input {:class "toggle"
                 :type "checkbox"
                 :checked done
                 :on-change #(toggle-done id)}]
        [:label {:on-double-click #(reset! editing true)} title]
        [:button.destroy {:on-click #(delete-todo id)}]]
       (when @editing
         [todo-input {:class "edit"
                      :title title
                      :on-save (fn [text] (save-todo id text))
                      :on-stop #(reset! editing false)}])])))

(defn task-list []
  (let [items (vals (:todos @db))
        filter-fn (case (:showing @db)
                    :done :done
                    :active (complement :done)
                    :all identity)
        visible-items (filter filter-fn items)
        all-completed? (every? :done items)]
    [:section.main
     [:input {:id "toggle-all"
              :class "toggle-all"
              :type "checkbox"
              :checked all-completed?
              :on-change #(complete-all-toggle)}]
     [:label {:for "toggle-all"} "Mark all as completed"]
     [:ul.todo-list
      (for [todo visible-items]
        ^{:key (:id todo)} [todo-item todo])]]))

(defn footer-controls []
  (let [items (vals (:todos @db))
        done-count (count (filter :done items))
        active-count (- (count items) done-count)
        props-for (fn [kw]
                    {:class (when (= kw (:showing @db)) "selected")
                     :on-click #(swap! db assoc :showing kw)
                     :href (str "#/" (name kw))})]
    [:footer.footer
     [:span.todo-count
      [:strong active-count] " " (case active-count 1 "item" "items") " left"]
     [:ul.filters
      [:li [:a (props-for :all) "All"]]
      [:li [:a (props-for :active) "Active"]]
      [:li [:a (props-for :done) "Completed"]]]
     (when (pos? done-count)
       [:button.clear-completed {:on-click clear-completed} "Clear completed"])]))

(defn app []
  [:div
   [:section.todoapp
    [task-entry]
    (when (seq (:todos @db))
      [:div
       [task-list]
       [footer-controls]])]
   [:footer.info
    [:p "Double-click to edit a to-do"]]])

;; ----- Render -----

(defn render []
  (rdom/render [app] (.getElementById js/document "root")))

(defn ^:export main []
  (local-storage->todos)
  (app-routes)
  (render))

(defn ^:dev/after-load reload! []
  (render))
