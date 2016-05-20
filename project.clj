(defproject imagionary "0.1.0-SNAPSHOT"
  :description "Simple web image dictionary for elementary school kids"
  :url "http://slovnik.didaktis.cz"
  :min-lein-version "2.0.0"
  :jvm-opts ["-Duser.timezone=UTC"]
  :dependencies [[cljs-ajax "0.5.2"]
                 [org.apache.httpcomponents/httpclient "4.5.1"]
                 [clj-brnolib "0.1.0-SNAPSHOT"
                  :exclusions [org.clojure/clojurescript]]
                 [compojure "1.5.0"]
                 [duct "0.6.1"]
                 [environ "1.0.3"]
                 [meta-merge "0.1.1"]
                 [ring-jetty-component "0.3.1"]
                 [org.slf4j/slf4j-nop "1.7.21"]
                 [duct/hikaricp-component "0.1.0"]
                 [com.h2database/h2 "1.4.191"]
                 [duct/ragtime-component "0.1.4"]
                 [secretary "1.2.3"]
                 [ring-middleware-format "0.7.0"]
                 [crypto-password "0.2.0"]]
  :plugins [[lein-environ "1.0.3"]]
  :main ^:skip-aot imagionary.main
  :target-path "target/%s/"
  :aliases {"run-task" ["with-profile" "+repl" "run" "-m"]
            "setup"    ["run-task" "dev.tasks/setup"]}
  :profiles
  {:dev  [:project/dev  :profiles/dev]
   :test [:project/test :profiles/test]
   :uberjar {:aot :all}
   :profiles/dev  {}
   :profiles/test {}
   :project/dev   {:dependencies [[duct/generate "0.6.1"]
                                  [reloaded.repl "0.2.1"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/tools.nrepl "0.2.12"]
                                  [eftest "0.1.1"]
                                  [kerodon "0.7.0"]]
                   :source-paths ["dev"]
                   :repl-options {:init-ns user}
                   :env {:port "3000"}}
   :project/test  {}})
