(ns ^:figwheel-load reacl-tutorial.core-test
  (:require [reacl.core :as reacl :include-macros true]
            [reacl.dom :as dom :include-macros true]
            [reacl.test-util :as reacl-test]
            [cljs.test :as t]
            [reacl-tutorial.core :refer (string-display 
                                         list-display
                                         contacts
                                         contacts-display
                                         ->NewText ->Add)])
  (:require-macros [cljs.test :refer (is deftest testing)]))

(enable-console-print!)

(deftest string-display-test
  (let [e (string-display "Hello, Mike")
        renderer (reacl-test/create-renderer)]
    (reacl-test/render! renderer e)
    (let [t (reacl-test/render-output renderer)]
      (is (reacl-test/dom=? (dom/h1 "Hello, Mike") t))
      (is (reacl-test/element-has-type? t :h1))
      (is (= ["Hello, Mike"] (reacl-test/element-children t))))))

(deftest list-display-test
  (let [e (list-display ["Lion" "Zebra" "Buffalo" "Antelope"])
        renderer (reacl-test/create-renderer)]
    (reacl-test/render! renderer e)
    (let [t (reacl-test/render-output renderer)]
      (is (reacl-test/element-has-type? t :ul))
      (doseq [c (reacl-test/element-children t)]
        (is (reacl-test/element-has-type? c :li))))))

(deftest contacts-display-handle-message-test
  (let [st (reacl-test/handle-message contacts-display [{:first "David" :last "Frese"}] [] "Foo"
                                      (->Add {:first "Mike" :last "Sperber"}))]
    (is (= [{:first "David", :last "Frese"} {:first "Mike", :last "Sperber"}]
           (:app-state st))))
  (let [st (reacl-test/handle-message contacts-display [{:first "David" :last "Frese"}] [] "Foo"
                                      (->NewText "David Frese"))]
    (is (= "David Frese"
           (:local-state st)))))

(deftest contacts-display-test
  (let [e (contacts-display contacts)
        renderer (reacl-test/create-renderer)]
    (reacl-test/render! renderer e)
    (let [t (reacl-test/render-output renderer)
          input (reacl-test/descend-into-element t [:div :div :input])
          st (reacl-test/invoke-callback input :onchange #js {:target #js {:value "Mike Sperber"}})]
      (is (= "Mike Sperber") (:local-state st)))
    (let [t (reacl-test/render-output renderer)
          button (reacl-test/descend-into-element t [:div :div :button])
          st (reacl-test/invoke-callback button :onclick #js {})]
      (is (= (conj contacts {:first "Mike", :last "Sperber"})
             (:app-state st))))
    (let [t (reacl-test/render-output renderer)]
      (is (reacl-test/element-has-type? t :div)))))

