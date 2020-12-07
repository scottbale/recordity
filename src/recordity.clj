(ns recordity
  (:require
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

(def delimiters {:pipe #"\|"
                 :comma #","
                 :space #" "})

(defrecord Record [lastname firstname gender color dob])

(defn record [lastname firstname gender color dob]
  (->Record lastname firstname gender color dob))

(defn parseRecord [delimiter ^String r]
  (apply ->Record (str/split r delimiter)))

(def comparators
  {:genderThenLastName 
   (fn [x y]
     ;; https://clojure.org/guides/comparators
     (compare [(:gender x) (:lastname x)]
              [(:gender y) (:lastname y)]))
   :lastNameDesc
   (fn [x y]
     (compare (:lastname y) (:lastname x)))})
