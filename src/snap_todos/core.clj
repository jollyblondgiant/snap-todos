(ns snap-todos.core
  (:require [datomic.api :as d]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [clojure.string :refer [join split]]
            [ring.middleware.params :refer [wrap-params]]
            [snap-todos.routes :as routes]
            ))


(defn handler [{route :uri params :params
                :as request}]
  (case route
    "/foo" (ring.util.response/response "bar")
    "/todos" (routes/todos request)
    "/login" (routes/login request)
    "/logout" (routes/logout request)
    (ring.util.response/not-found)))

(defn -main
  [& args]
  (run-jetty
   (-> handler
       wrap-params
       wrap-cookies)
   {:port 3000}))
