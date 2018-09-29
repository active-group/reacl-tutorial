(ns reacl-tutorial.core
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom :include-macros true]
            [clojure.string :as string]))

(enable-console-print!)

(println "Hello world!")

(def contacts
  [{:first "Ben" :last "Bitdiddle" :email "benb@mit.edu"}
   {:first "Alyssa" :middle-initial "P" :last "Hacker" :email "aphacker@mit.edu"}
   {:first "Eva" :middle "Lu" :last "Ator" :email "eval@mit.edu"}
   {:first "Louis" :last "Reasoner" :email "prolog@mit.edu"}
   {:first "Cy" :middle-initial "D" :last "Effect" :email "bugs@mit.edu"}
   {:first "Lem" :middle-initial "E" :last "Tweakit" :email "morebugs@mit.edu"}])

(defn middle-name [{:keys [middle middle-initial]}]
  (cond
    middle (str " " middle)
    middle-initial (str " " middle-initial ".")))

(defn display-name
  [{:keys [first last] :as contact}]
  (str last ", " first (middle-name contact)))

(defn parse-contact [contact-str]
  (let [[first middle last :as parts] (string/split contact-str #"\s+")
        [first last middle] (if (nil? last) [first middle] [first last middle])
        middle (when middle (string/replace middle "." ""))
        c (if middle (count middle) 0)]
    (when (>= (count parts) 2)
      (cond-> {:first first :last last}
        (== c 1) (assoc :middle-initial middle)
        (>= c 2) (assoc :middle middle)))))

(defrecord Registry [people classes])

(def registry
  (->Registry
   [{:type :student :first "Ben" :last "Bitdiddle" :email "benb@mit.edu"}
    {:type :student :first "Alyssa" :middle-initial "P" :last "Hacker"
     :email "aphacker@mit.edu"}
    {:type :professor :first "Gerald" :middle "Jay" :last "Sussman"
     :email "metacirc@mit.edu" :classes [:6001 :6946]}
    {:type :student :first "Eva" :middle "Lu" :last "Ator" :email "eval@mit.edu"}
    {:type :student :first "Louis" :last "Reasoner" :email "prolog@mit.edu"}
    {:type :professor :first "Hal" :last "Abelson" :email "evalapply@mit.edu"
     :classes [:6001]}]
  {:6001 "The Structure and Interpretation of Computer Programs"
   :6946 "The Structure and Interpretation of Classical Mechanics"
   :1806 "Linear Algebra"}))

(reacl/defclass demo
  this []
  render
  (dom/h1 "Reacl Tutorial"))

(reacl/defclass string-display
  this s []
  render (dom/h1 s))

(defrecord Delete [contact])

(reacl/defclass contact-display
  this contact [parent]
  render
  (dom/li
   (dom/span (display-name contact))
   (dom/button
    {:onclick (fn [_]
                (reacl/send-message! parent (->Delete contact)))}
    "Delete")))

(defrecord NewText [text])
(defrecord Add [contact])

(reacl/defclass contacts-display
  this contacts []
  local-state [new-text ""]
  render
  (dom/div
   (dom/h2 "Contact list")
   (dom/ul
    (map (fn [c] (contact-display c this)) contacts))
   (dom/div
    (dom/input {:type "text" :value new-text
                :onchange
                (fn [e]
                  (reacl/send-message! this
                                       (->NewText (.. e -target -value))))})
    (dom/button {:onclick
                 (fn [e]
                   (reacl/send-message! this
                                        (->Add (parse-contact new-text))))}
                "Add contact")))
  handle-message
  (fn [msg]
    (cond
      (instance? Delete msg)
      (let [d (:contact msg)]
        (reacl/return :app-state
                      (vec (remove (fn [c] (= c d)) contacts))))
      (instance? NewText msg)
      (reacl/return :local-state (:text msg))
      (instance? Add msg)
      (reacl/return :app-state (conj contacts (:contact msg))
                    :local-state ""))))

(defn student-view
  [p classes]
  (dom/li (display-name p)))

(defn professor-view
  [p classes]
  (dom/li
   (dom/div (display-name p))
   (dom/label "Classes")
   (dom/ul
    (map dom/li (map classes (:classes p))))))

(defn person-view
  [p classes]
  (case (:type p)
    :student
    (student-view p classes)
    :professor
    (professor-view p classes)))

(reacl/defclass people-display
  this rg []
  render
  (dom/div
   (dom/h2 "Registry")
   (map (fn [p] (person-view p (:classes rg))) (:people rg))))

(defrecord EditableLocalState [text editing?])

(defn display-if
  [b]
  (if b
    {}
    {:style {:display "none"}}))

(defrecord Editing [])
(defrecord CommitEdit [])

(reacl/defclass editable-text
  this text []
  local-state [local-state (->EditableLocalState text false)]

  render
  (let [editing? (:editing? local-state)]
    (dom/li
     (dom/span (display-if (not editing?))
               (:text local-state))
     (dom/input (merge (display-if editing?)
                       {:value (:text local-state)
                        :onchange
                         (fn [e]
                           (reacl/send-message! this
                                                (->NewText (.. e -target -value))))
                        :onkeydown (fn [e]
                                     (when (= (.-key e) "Enter")
                                       (reacl/send-message! this (->CommitEdit))))}))
                
     (dom/button (merge (display-if (not editing?))
                        {:onclick
                         (fn [e]
                           (reacl/send-message! this (->Editing)))})
                 "Edit")))
  handle-message
  (fn [msg]
    (cond
      (instance? Editing msg)
      (reacl/return :local-state
                    (assoc local-state :editing? true))
      (instance? NewText msg)
      (reacl/return :local-state
                    (assoc local-state :text (:text msg)))
      (instance? CommitEdit msg)
      (reacl/return :local-state
                    (assoc local-state :editing? false)
                    :app-state
                    (:text local-state)))))

(defrecord NewName [key name])

(reacl/defclass classes-display
  this classes []
  render
  (dom/div
   (dom/h2 "Classes")
   (map (fn [[key name]] (editable-text
                          (reacl/opt :reaction
                                     (reacl/reaction this
                                                     (partial ->NewName key)))
                          name))
        classes))
  handle-message
  (fn [msg]
    (reacl/return :app-state
                  (assoc classes (:key msg) (:name msg)))))
          
(reacl/defclass registry-display
  this rg []
  render
  (do
    (println rg)
  (dom/div
   (people-display rg)
   (classes-display (reacl/opt :embed-app-state
                               (fn [rg new-classes]
                                 (assoc rg :classes new-classes)))
                    (:classes rg)))))
   
(reacl/render-component
 (.getElementById js/document "content")
 registry-display registry)
