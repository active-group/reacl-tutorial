(ns reacl-tutorial.core
  (:require [reacl2.core :as reacl :include-macros true]
            [reacl2.dom :as dom :include-macros true]
            [clojure.string :as string]))

(enable-console-print!)

(println "Hello world!")

(reacl/defclass string-display
  this s []
  render
  (dom/h1 s)
  handle-message
  (fn [new]
    (reacl/return :app-state new)))

(defn stripe
  [bgc text]
  (dom/li {:style {:background-color bgc}} text))

(reacl/defclass list-display
  this lis []
  render
  (dom/ul {:class "animals"}
   (map stripe (cycle ["#ff0" "#fff"]) lis)))

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

#_(reacl/defclass contact-display
  this contact [parent] ; parent later
  render
  (dom/li
   (dom/span (display-name contact))
   (dom/button {:onclick (fn [e] (reacl/send-message! parent contact))} "Delete"))) ; add later

(defrecord Delete [contact])

(reacl/defclass contact-display
  this contact [parent] ; parent later
  render
  (dom/li
   (dom/span (display-name contact))
   (dom/button {:onclick (fn [e] (reacl/send-message! parent (->Delete contact)))} "Delete")))

#_(reacl/defclass contacts-display
  this data []
  render
  (dom/div
   (dom/h2 "Contact list")
   (dom/ul
    (map (fn [c] (contact-display c this)) data)))
  handle-message
  (fn [msg]
    (reacl/return :app-state
                  (vec (remove (fn [c] (= c msg)) data)))))

(defrecord NewText [text])
(defrecord Add [contact])

(reacl/defclass contacts-display
  this data []
  local-state [new-text ""]
  render
  (dom/div
   (dom/h2 "Contact list")
   (dom/ul
    (map (fn [c] (contact-display c this)) data))
   (dom/div
    (dom/input {:type "text" :value new-text
                :onchange (fn [e] (reacl/send-message! this
                                                       (->NewText (.. e -target -value))))})
    (dom/button {:onclick (fn [e] (reacl/send-message! this (->Add (parse-contact new-text))))} "Add contact")))
  handle-message
  (fn [msg]
    (println "Msg" msg)
    (cond
      (instance? Delete msg)
      (reacl/return :app-state
                    (vec (remove (fn [c] (= c (:contact msg))) data)))

      (instance? NewText msg)
      (reacl/return :local-state (:text msg))
      
      (instance? Add msg)
      (reacl/return :app-state (conj data (:contact msg))
                    :local-state ""))))


(def registry
  {:people
   [{:type :student :first "Ben" :last "Bitdiddle" :email "benb@mit.edu"}
    {:type :student :first "Alyssa" :middle-initial "P" :last "Hacker"
     :email "aphacker@mit.edu"}
    {:type :professor :first "Gerald" :middle "Jay" :last "Sussman"
     :email "metacirc@mit.edu" :classes [:6001 :6946]}
    {:type :student :first "Eva" :middle "Lu" :last "Ator" :email "eval@mit.edu"}
    {:type :student :first "Louis" :last "Reasoner" :email "prolog@mit.edu"}
    {:type :professor :first "Hal" :last "Abelson" :email "evalapply@mit.edu"
     :classes [:6001]}]
   :classes
   {:6001 "The Structure and Interpretation of Computer Programs"
    :6946 "The Structure and Interpretation of Classical Mechanics"
    :1806 "Linear Algebra"}})

(defn student-view
  [student]
  (dom/li (display-name student)))

(defn professor-view
  [professor]
  (dom/li
   (dom/div (display-name professor))
   (dom/label "Classes")
   (dom/ul
    (map dom/li (:classes professor)))))

(defmulti entry-view :type)

(defmethod entry-view :student
  [person]
  (student-view person))

(defmethod entry-view :professor
  [person]
  (professor-view person))

(defn people [data]
  (->> data
    :people
    (mapv (fn [x]
            (if (:classes x)
              (update-in x [:classes]
                (fn [cs] (mapv (:classes data) cs)))
               x)))))

(reacl/defclass registry-display
  this data []
  render
  (dom/div
   (dom/h2 "Registry")
   (dom/ul
    (map entry-view (people data)))))

(defn display [show]
  (if show
    {}
    {:display "none"}))

(defrecord EditableLocalState
  [text editing?])

(defrecord Editing [])
(defrecord CommitEdit [])

(reacl/defclass editable
  this text []

  local-state [local-state (->EditableLocalState text false)]
  
  render
  (let [editing? (:editing? local-state)
        ttext (:text local-state)]
    (dom/li
     (dom/span {:style (display (not editing?))}
               text)
     (dom/input {:style (display editing?)
                 :value ttext
                 :onchange (fn [e] (reacl/send-message! this (->NewText (.. e -target -value))))
                 :onkeydown (fn [e]
                              (when (= (.-key e) "Enter")
                                (reacl/send-message! this (->CommitEdit))))
                 :onblur (fn [e]
                           (reacl/send-message! this (->CommitEdit)))})
     (dom/button {:style (display (not editing?))
                  :onclick (fn [e] (reacl/send-message! this (->Editing)))}
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
                    
(defrecord ChangeClassName [key name])

(reacl/defclass classes-display
  this data []

  render
  (dom/div {:id "classes"}
           (dom/h2 "Classes")
           (map (fn [[key name]] (editable (reacl/opt :reaction (reacl/reaction this (fn [name] (->ChangeClassName key name)))) name))
                (:classes data)))

  handle-message
  (fn [msg]
    (cond
      (instance? ChangeClassName msg)
      (reacl/return :app-state
                    (assoc-in data [:classes (:key msg)] (:name msg))))))
      
;; string-display
#_ (def top
  (reacl/render-component
   (.getElementById js/document "content")
   string-display "Hello world!" reacl/no-reaction))

;; list-display
#_(def top
  (reacl/render-component
   (.getElementById js/document "content")
   list-display ["Lion" "Zebra" "Buffalo" "Antelope"] reacl/no-reaction))

;; contacts
#_(def top
  (reacl/render-component
   (.getElementById js/document "content")
   contacts-display contacts reacl/no-reaction))

;; registry
#_(def top
  (reacl/render-component
   (.getElementById js/document "content")
   registry-display registry reacl/no-reaction))

;; registry + classes

(def registry-top
  (reacl/render-component
   (.getElementById js/document "registry")
   registry-display registry reacl/no-reaction))

(def classes-top
  (reacl/render-component
   (.getElementById js/document "classes")
   classes-display registry reacl/no-reaction))

