(defproject recordity "0.0.1"
  :description "coding exercise"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v20.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.clojure/tools.logging "1.1.0"]
                 [cheshire "5.10.0"]
                 [clojure.java-time "0.3.2"]
                 [compojure "1.6.2" :exclusions [ring/ring-core]]
                 [ring/ring-core "1.8.2"]
                 ;; [ring/ring-defaults "0.3.2"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [org.apache.logging.log4j/log4j-core "2.13.3"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.13.3"]]
  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]

  :profiles {:dev {:resource-paths ["test-data"]
                   :dependencies [[ring/ring-mock "0.4.0"]]}}
  :main cli

  ;;:plugins [[lein-ring "0.12.5"]]
  ;;:ring {:handler ring.core/handler}
)
