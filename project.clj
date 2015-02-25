(defproject theladders/rmq-producer "0.2.1-SNAPSHOT"
  :description "Read values from stdin and put them on a RabbitMQ queue."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.2.4"]
                 [com.novemberain/langohr "1.4.1"]]
  :aot :all

  :plugins [[theladders/lein-uberjar-deploy "0.1.2"]]

  :repositories [["snapshots" {:id "nexus" :url "http://mercurial:8081/nexus/content/repositories/snapshots"}]
                 ["releases"  {:id "nexus" :url "http://mercurial:8081/nexus/content/repositories/releases"}]
                ]


  :main rmq-producer.main)
