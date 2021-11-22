(ns snap-todos.graph
  (:require [oz.core :as oz]))

;;https://github.com/metasoarous/oz
(defn task-graph
  ""
  [tasks]
  (oz/view! {:data {:values
                    (->> tasks
                         (map (fn [[tx tasks]]
                                {:time tx :quantity tasks})))}
             :encoding {:x {:field "time" :type "quantitative"}
                        :y {:field "tasks" :type "quantitative"}}
             :mark "line"}))

(comment
  (oz/start-server!)
  (oz/view! line-plot))
