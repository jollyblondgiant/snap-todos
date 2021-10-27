(ns snap-todos.routes
  (:require [snap-todos.todos :as todos]
            [snap-todos.graph :refer [task-graph]]
            [clojure.string :refer [join]]))

(defn login
  "validates email
  and adds username cookie
  then redirects home"
  [{route :uri params :params
    :as request}]
  (let [email #"[a-zA-z0-9+_.-]+@[a-zA-Z0-9]+.[a-z]+"]
    (if-let [user (->> "user" (get params) (re-matches email))]
      (-> (ring.util.response/redirect "/")
          (ring.util.response/set-cookie
           "user" user {:max-age 300}))
      (ring.util.response/bad-request))))

(defn logout
  "prints cheerful message
  and resets user's cookie store
  thanks for todoing!"
  [request]
  (->
   (ring.util.response/response "successfully logged out!")
   (ring.util.response/set-cookie "user" "logout" {:max-age 0})))

(defn home
  "checks user cookies
  and returns api links
  in html"
  [{route :uri params :params
    :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (-> (str "logged in as user: " user "<br/>"
             "available actions:" "<br/>"
             "<a href='/todos'>get-todos</a>" "<br/>"
             "<a href='/dones'>completed-todos</a>" "<br/>"
             "<a href='/graph'>task burndown</a>" "<br/>"
             "<a href='/chart'>burndown (visualized)</a></br>"
             "<a href='/logout'>logout</a>" )
        ring.util.response/response
        (ring.util.response/content-type "text/html"))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))

(def nav
  '("<a href='/logout'>log out</a>" "|"
    "<a href='/'>home</a>"          "<br/>"
    "<span>add new todo by navigating to "
    "localhost:3000/add-todo?todo={description}</span>")
  )

(defn dones
  "gets completed tasks
  and returns html
  with tasks and actions"
  [{route :uri params :params
    :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [todos (todos/get-dones-by-user user)
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
  "gets incomplete tasks
  and returns html
  with tasks and actions"
  [{route :uri params :params
              :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [todos (todos/get-todos-by-user user)
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
  "sends assert request
  marking todo as complete
  and then redirects"
  [{route :uri params :params
    :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [id (get params "id")]
      (todos/complete-todo id)
      (ring.util.response/redirect "/todos"))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))

(defn incomplete-todo
  "sends assert request
  marking task as incomplete
  and then redirects"
  [{route :uri params :params
    :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [id (get params "id")]
      (todos/incomplete-todo id)
      (ring.util.response/redirect "/dones"))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))

(defn forget-todo
  "sends retract request
  removing task completely
  and then redirects"
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
  "takes description string
   makes assert request with it
  and then redirects"
  [{route :uri params :params:as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [description (get params "todo")]
      (todos/add-new-todo user description)
      (ring.util.response/redirect "/todos"))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))

(defn todo-chart
  "gets task history
  and formats it to table
  in html"
  [{route :uri params :params
    :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [chart (todos/tx-graph-for-user user)
          row (fn [[time count]]
               (-> "<tr><td>%s</td><td>%s</td></tr>"
                   (format time count)))]
      (-> (ring.util.response/response
           (->> chart
                (map row) join
                (format
                 "<table><tr>Transaction<th></th>Tasks<th></th></tr>%s</table>" )
                (str (join nav))))
          (ring.util.response/content-type "text/html")))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))

(defn todo-graph
  "gets task history
  and sends to task-graph to be
  rendered in new tab"
  [{route :uri params :params
    :as request}]
  (if-let [user (-> request :cookies (get "user") :value)]
    (let [chart (todos/tx-graph-for-user user)]
      (task-graph chart)
      (ring.util.response/redirect "/"))
    (ring.util.response/response
     "please login at /login?user={email@domain.eg}")))
