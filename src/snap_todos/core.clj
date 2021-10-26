(ns snap-todos.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [clojure.string :refer [join split]]
            [ring.middleware.params :refer [wrap-params]]
            [snap-todos.routes :as routes]
            ))


(defn handler [{route :uri params :params
                :as request}]
  (case route
    "/" (routes/home request)
    "/login" (routes/login request)
    "/todos" (routes/todos request)
    "/logout" (routes/logout request)
    "/complete-todo" (routes/complete-todo request)
    "/forget-todo" (routes/forget-todo request)
    "/add-todo" (routes/add-todo request)
    (ring.util.response/not-found)))

(defn -main
  [& args]
  (run-jetty
   (-> handler
       wrap-params
       wrap-cookies)
   {:port 3000}))
