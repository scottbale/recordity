(ns recordity-test
  (:use
   [clojure.test])
  (:require
   [clojure.tools.logging :as log]
   [recordity :refer :all]))

(deftest testParsePipeDelimited
  (is (= {:lastname "Smith"
          :firstname "John"
          :gender "m"
          :favcolor "green"
          :dob "1/2/53"}
         (parseRecord (delimiters :pipe) "Smith|John|m|green|1/2/53"))))

(deftest testParseCommaDelimited
  (is (= {:lastname "Smith"
          :firstname "John"
          :gender "m"
          :favcolor "green"
          :dob "1/2/53"}
         (parseRecord (delimiters :comma) "Smith,John,m,green,1/2/53"))))

(deftest testParseSpaceDelimited
  (is (= {:lastname "Smith"
          :firstname "John"
          :gender "m"
          :favcolor "green"
          :dob "1/2/53"}
         (parseRecord (delimiters :space) "Smith John m green 1/2/53"))))
