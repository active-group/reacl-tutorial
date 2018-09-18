(defproject reacl-tutorial "0.1.0-SNAPSHOT"
  :description "Reacl tutorial"
  :url "https://github.com/active-group/reacl-tutorial"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [reacl "2.0.2"]

                 [com.bhauman/figwheel-main "0.1.9"]
                 [com.bhauman/rebel-readline-cljs "0.1.4"]]

  :resource-paths ["resources" "target"]

  :paths ["resources" "target" "src" "tests"]
  
  :clean-targets ^{:protect false} ["resources/public"]

  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]
            "build-dev" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]}

  ;; FIXME: test?

  )
