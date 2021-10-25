(ns snap-todos.todos
  (:require [datomic.api :as d]))

(def db-uri "datomic:mem://foo")

(def todo-schema [{:db/ident :todo/description
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/doc "a description of the todo"}
                  {:db/ident :todo/creator
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/doc "the creator of the todo"}
                  {:db/ident :todo/complete
                   :db/valueType :db.type/boolean
                   :db/cardinality :db.cardinality/one
                   :db/doc "whether the todo is complete"}])


(def default-todos [{:todo/description "set up http server"
                     :todo/complete true
                     :todo/creator "andy@apogenius.com"}
                    {:todo/description "create CRUD ops on todos"
                     :todo/complete false
                     :todo/creator "andy@apogenius.com"}
                    {:todo/description "setup user login"
                     :todo/complete true
                     :todo/creator "andy@apogenius.com"}
                    {:todo/description "create graphing for todos"
                     :todo/complete false
                     :todo/creator "andy@apogenius.com"}
                    {:todo/description "create login query"
                     :todo/complete true
                     :todo/creator "andy@apogenius.com"}
                    {:todo/description "create logout query"
                     :todo/complete false
                     :todo/creator "andy@apogenius.com"}
                    {:todo/description "write really good documentation"
                     :todo/creator "andy@apogenius.com"
                     :todo/complete false}])

(d/create-database db-uri)
(defonce conn (d/connect db-uri))
(map (fn [tx] @(d/transact conn tx)) [todo-schema default-todos])
@(d/transact conn todo-schema)
@(d/transact conn default-todos)

(defn get-todos-by-user
  ""
  [user]
  (d/q '[:find ?description
         :in $ ?user
         :where [?todo :todo/creator ?user]
         [?todo :todo/complete false]
         [?todo :todo/description ?description]]
       (d/db conn)
       user))


(defn get-todos
  ""
  []
  (d/q
   '[:find ?description
     :where
     [?todo :todo/description ?description]]))


(defn get-completed-todos
  ""
  []
  (d/q
   '[:find ?description
     :where
     [?todo :todo/complete true]
     [?todo :todo/description ?description]]))

(defn get-completed-todos-by-user
  ""
  [user]
  (d/q
   '[:find ?description
     :in $ ?user
     :where [?todo :todo/creator ?user]
     [?todo :todo/complete true]
     [?todo :todo/description ?description]]
   (d/db conn)
   user)
  )


(defn todo-history [user]
  (d/q
   '[:find ?description ?complete ?tx ?op
     :in $ ?user
     :where
     [?todo :todo/complete ?complete ?tx ?op]
     [?todo :todo/creator ?user]
     [?todo :todo/description ?description]]
   (-> conn d/db d/history)
   user
   ))
