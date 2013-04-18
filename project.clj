(defproject tbn "0.0.1-SNAPSHOT"
  :min-lein-version "2.0.0"
  :plugins         [[lein-cljsbuild "0.3.0"]]
  :dependencies    [[org.clojure/clojure "1.4.0"]
                    [cljasmine           "0.1.0-SNAPSHOT"]]
  :cljsbuild
  {:crossovers     []
   :crossover-path "crossover"
   :builds
   [{:source-paths ["src" "test"],
     :id "test",
     :compiler
     {:pretty-print  true,
      :output-to     "specs/unit_spec.js",
      :optimizations :simple
      :foreign-libs  [{:file     "libs/async.js" 
                       :provides ["async"]}]}}]})
