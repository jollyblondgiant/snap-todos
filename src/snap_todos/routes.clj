(ns snap-todos.routes
  (:require [snap-todos.todos :as todos]
            [clojure.string :refer [join]]))

(defn login
  ""
  [{route :uri params :params
              :as request}]
  (if-let [user (->> "user" (get params)
                     (re-matches #"[a-zA-z0-9+_.-]+@[a-zA-Z0-9]+.[a-z]+"))]
    (-> (str "successfully logged in!" "\n"
             "as user: " user "\n"
             "available actions:" "\n"
             "/get-todos" "\n"
             "/view-graph" "\n"
             "/add-todo" "\n"
             "/forget-todo/{todo-description}" "\n"
             "/complete-todo/{todo-description}" "\n")
        ring.util.response/response
        (ring.util.response/set-cookie
         "user" user {:max-age 300}))
    (ring.util.response/bad-request)))

(defn logout
  ""
  [request]
  (->
   (ring.util.response/response "successfully logged out!")
   (ring.util.response/set-cookie "user" "logout" {:max-age 0})))

(defn todos
  ""
  [{route :uri params :params
              :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (ring.util.response/response
     (join "\n" (todos/get-todos-by-user user)))
    (ring.util.response/response "please login to make queries")))
