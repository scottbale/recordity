(ns recordity
  (:require
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [java-time :as jt]))

(def delimiters {:pipe #"\|"
                 :comma #","
                 :space #" "})

(def canonical-date-format "M/d/yyyy")

;; just in case input format is different than canonical
(def input-date-format "yyyy/M/d")

(defrecord Record [lastname firstname gender color dob])

(defn record [lastname firstname gender color dob]
  (->Record lastname firstname gender color dob))

(defn record-str [{:keys [lastname firstname gender color dob]}]
  (apply str (interpose " " [lastname firstname gender color (jt/format canonical-date-format dob)])))

(defn parse-date
  "TODO"
  ([^String date]
   (parse-date input-date-format date))
  ([^String date-format ^String date]
   (jt/local-date date-format date)))

(defn parse-record [delimiter ^String dob-format ^String r]
  (let [[l f g c d] (map str/trim (str/split r delimiter))]
    (->Record l f g c (parse-date dob-format d))))

(def comparators
  {:genderThenLastName 
   (fn [x y]
     ;; https://clojure.org/guides/comparators
     (compare [(:gender x) (:lastname x)]
              [(:gender y) (:lastname y)]))
   :lastNameDesc
   (fn [x y]
     (compare (:lastname y) (:lastname x)))
   :dob
   (fn [x y]
     (compare (:dob x) (:dob y)))})
