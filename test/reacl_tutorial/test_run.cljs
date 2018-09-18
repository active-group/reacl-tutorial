(ns reacl-tutorial.test-run
  (:require [cljs.test :refer-macros [run-tests run-all-tests]]
            [reacl-tutorial.core-test]))

(enable-console-print!)

(run-tests 'reacl-tutorial.core-test)

