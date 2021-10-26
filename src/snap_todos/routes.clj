(ns snap-todos.routes
  (:require [snap-todos.todos :as todos]
            [clojure.string :refer [join]]))

(defn login
  ""
  [{route :uri params :params
    :as request}]
  (let [email #"[a-zA-z0-9+_.-]+@[a-zA-Z0-9]+.[a-z]+"]
    (if-let [user (->> "user" (get params) (re-matches email))]
      (-> (ring.util.response/redirect "/")
          (ring.util.response/set-cookie
           "user" user {:max-age 300}))
      (ring.util.response/bad-request))))

(defn logout
  ""
  [request]
  (->
   (ring.util.response/response "successfully logged out!")
   (ring.util.response/set-cookie "user" "logout" {:max-age 0})))

(defn home
  ""
  [{route :uri params :params
    :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (-> (str "logged in as user: " user "<br/>"
             "available actions:" "<br/>"
             "<a href='/todos'>get-todos</a>" "<br/>"
             "<a href='/dones'>completed-todos</a>" "<br/>"
             "<a href='/graph'>view-graph</a>" "<br/>"
             "<a href='/logout'>logout</a>" )
        ring.util.response/response
        (ring.util.response/content-type "text/html"))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))

(defn dones
  ""
  [{route :uri params :params
    :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [nav
          '("<a href='/logout'>log out</a>" "|"
            "<a href='/'>home</a>"          "<br/>"
            "<span>add new todo by navigating to "
            "localhost:3000/add-todo?todo={description}</span>")
          todos (todos/get-dones-by-user user)
          todo (fn [[label id]]
                 (str "<span>" label  "</span>"
                      " <a href='/incomplete-todo?id=" id
                      "'>mark as incomplete</a> "
                      " <a href='/forget-todo?id=" id
                      "'>forget task</a> "
                      "<br/>"))]
      (-> (ring.util.response/response
           (->> todos (map todo) (into nav) join))
          (ring.util.response/content-type "text/html")))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))

(defn todos
  ""
  [{route :uri params :params
              :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [nav
          '("<a href='/logout'>log out</a>" "|"
            "<a href='/'>home</a>"          "<br/>"
            "<span>add new todo by navigating to "
            "localhost:3000/add-todo?todo={description}</span>")
          todos (todos/get-todos-by-user user)
          todo (fn [[label id]]
                 (str "<span>" label  "</span>"
                      " <a href='/complete-todo?id=" id
                      "'>mark as complete</a> "
                      " <a href='/forget-todo?id=" id
                      "'>forget task</a> "
                      "<br/>"))]
      (-> (ring.util.response/response
           (->> todos (map todo) (into nav) join))
          (ring.util.response/content-type "text/html")))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))

(defn complete-todo
  ""
  [{route :uri params :params
    :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [id (get params "id")]
      (todos/complete-todo id)
      (ring.util.response/redirect "/todos"))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))

(defn incomplete-todo
  ""
  [{route :uri params :params
    :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [id (get params "id")]
      (todos/incomplete-todo id)
      (ring.util.response/redirect "/dones"))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))

(defn forget-todo
  ""
  [{route :uri params :params
    :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [id (get params "id")]
      (-> id
          todos/get-todo ffirst
          todos/remove-todo )
      (ring.util.response/redirect "/todos"))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))

(defn add-todo
  ""
  [{route :uri params :params
    :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [description (get params "todo")]
      (todos/add-new-todo user description)
      (ring.util.response/redirect "/todos"))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))
