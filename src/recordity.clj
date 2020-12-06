(ns recordity
  (:require
   [clojure.string :as str]
   [clojure.tools.logging :as log]))

(def delimiters {:pipe #"\|"
                 :comma #","
                 :space #" "})

(defn parseRecord [delimiter ^String r]
  (zipmap [:lastname :firstname :gender :favcolor :dob] (str/split r delimiter)))
