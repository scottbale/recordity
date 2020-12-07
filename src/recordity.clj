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
