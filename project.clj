(defproject fogus/phenomena "0.1.0-SNAPSHOT"
  :description "TODO"
  :url "http://www.github.com/fogus/phenomena"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0-master-SNAPSHOT"]]
  :plugins [[lein-marginalia "0.8.0"]
            [org.clojure/test.check "0.7.0"]]
  :source-paths ["src/clj"]
  :test-paths ["test"]
  :repositories [["snapshots" "https://oss.sonatype.org/content/repositories/snapshots/"]])
