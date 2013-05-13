(defproject tbn "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :hooks           [leiningen.cljsbuild]
  :plugins         [[lein-cljsbuild "0.3.0"]]
  :dependencies    [[org.clojure/clojure "1.4.0"]]
  :profiles        {:dev {:dependencies [[mocha-latte "0.1.1"]
                                         [chai-latte  "0.2.0-SNAPSHOT"]]}}
  :cljsbuild
  {:crossovers     []
   :crossover-path "crossover"
   :builds
   [{:source-paths ["src" "test"],
     :id "test",
     :compiler
     {:pretty-print  true,
      :output-to     "test/unit.js",
      :optimizations :simple
      :foreign-libs  [{:file     "libs/async.js"
                       :provides ["async"]}]}}]})
