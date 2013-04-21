(defproject tbn "0.0.1-SNAPSHOT"
  :min-lein-version "2.0.0"
  :plugins         [[lein-cljsbuild "0.3.1"]]
  :dependencies    [[org.clojure/clojure "1.4.0"]
                    [cljasmine           "0.1.0-SNAPSHOT"]]
  :cljsbuild
  {:crossovers     []
   :crossover-path "crossover"
   :test-commands
     {"unit" ["phantomjs"
              "spec/javascripts/support/phantom/phantomjs-testrunner.js"
              "spec/html/PhantomRunner.html"]}
   :builds
   [{:source-paths ["src" "test"],
     :id "test",
     :compiler
     {:pretty-print  true,
      :output-to     "spec/unit_spec.js",
      :optimizations :simple
      :foreign-libs  [{:file     "libs/async.js" 
                       :provides ["async"]}]}}]})
