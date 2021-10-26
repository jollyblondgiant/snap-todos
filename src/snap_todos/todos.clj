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
                   :db/doc "whether the todo is complete"}
                  {:db/ident :todo/id
                   :db/valueType :db.type/uuid
                   :db/cardinality :db.cardinality/one
                   :db/doc "for updating or deleting tasks"
                   }])


(def default-todos [{:todo/description "set up http server"
                     :todo/complete true
                     :todo/id (d/squuid)
                     :todo/creator "andy@apogenius.com"}
                    {:todo/description "create CRUD ops on todos"
                     :todo/complete true
                     :todo/id (d/squuid)
                     :todo/creator "andy@apogenius.com"}
                    {:todo/description "setup user login"
                     :todo/complete true
                     :todo/id (d/squuid)
                     :todo/creator "andy@apogenius.com"}
                    {:todo/description
                     "create graphing for todos"
                     :todo/complete false
                     :todo/id (d/squuid)
                     :todo/creator "andy@apogenius.com"}
                    {:todo/description "create login query"
                     :todo/complete true
                     :todo/id (d/squuid)
                     :todo/creator "andy@apogenius.com"}
                    {:todo/description "create logout query"
                     :todo/complete true
                     :todo/id (d/squuid)
                     :todo/creator "andy@apogenius.com"}
                    {:todo/description
                     "write really good documentation"
                     :todo/creator "andy@apogenius.com"
                     :todo/id (d/squuid)
                     :todo/complete false}])

(d/create-database db-uri)
(defonce conn (d/connect db-uri))
(map (fn [tx] @(d/transact conn tx)) [todo-schema default-todos])
@(d/transact conn todo-schema)
@(d/transact conn default-todos)

(defn add-new-todo
  ""
  [user description]
  (->> [{:todo/creator user
         :todo/description description
         :todo/id (d/squuid)
         :todo/complete false}]
       (d/transact conn)))

(defn remove-todo
  ""
  [uuid]
  (->> [:db/retractEntity [:todo/id uuid]]
       (d/transact conn)))

(defn complete-todo
  ""
  [uuid]
  (->> [{:db/id [:todo/id uuid]
         :todo/complete true}]
       (d/transact conn)))

(defn get-todos-by-user
  ""
  [user]
  (d/q '[:find ?description ?todo
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

(defn tx-history []
  (d/q
   '[:find ?tx
     :where
     [?tx :db/txInstant]]
   (d/db conn)))

(defn todo-snapshot
  [user tx]
  (d/q
   '[:find ?description
     :in $ ?user
     :where [?todo :todo/creator ?user]
     [?todo :todo/complete false]
     [?todo :todo/description ?description]]
   (d/as-of (d/db conn) tx)
   user))

(defn tx-graph-for-user [user]
  (->> (tx-history)
       (map (fn [txs]
              (first txs)))
       sort
       (reduce
        #(conj %1
               [%2 (count (todo-snapshot user %2))]
               ) []
        )))

(defn todo-history [user]
  (d/q
   '[:find ?description ?complete ?tx ?op
     :in $ ?user
     :where
     [?todo :todo/complete ?complete ?tx ?op]
     [?todo :todo/creator ?user]
     [?todo :todo/description ?description]]
   (-> conn d/db d/history)
   user))
