(ns recordity-test
  (:use
   [clojure.test])
  (:require
   [clojure.tools.logging :as log]
   [recordity :refer :all]))

(deftest testParsePipeDelimited
  (is (= (record "Smith" "John" "m" "green" "1/2/53")
         (parseRecord (delimiters :pipe) "Smith|John|m|green|1/2/53"))))

(deftest testParseCommaDelimited
  (is (= (record "Smith" "John" "m" "green" "1/2/53")
         (parseRecord (delimiters :comma) "Smith,John,m,green,1/2/53"))))

(deftest testParseSpaceDelimited
  (is (= (record "Smith" "John" "m" "green" "1/2/53")
         (parseRecord (delimiters :space) "Smith John m green 1/2/53"))))

(deftest testSortByGenderThenLastName
  (is (= [
          (record "Kelliot" "Kris" "f" "red" "2/13/61")
          (record "Jabar" "Aaron" "m" "blue" "8/12/55")
          (record "Smith" "John" "m" "green" "1/2/53")
          ]

         (sort
          (comparators :genderThenLastName)
          [(record "Smith" "John" "m" "green" "1/2/53")
           (record "Kelliot" "Kris" "f" "red" "2/13/61")
           (record "Jabar" "Aaron" "m" "blue" "8/12/55")])
         )))


(deftest testSortByLastNameDesc
  (is (= [
          (record "Smith" "John" "m" "green" "1/2/53")
          (record "Kelliot" "Kris" "f" "red" "2/13/61")
          (record "Jabar" "Aaron" "m" "blue" "8/12/55")
          ]

         (sort
          (comparators :lastNameDesc)
          [(record "Smith" "John" "m" "green" "1/2/53")
           (record "Jabar" "Aaron" "m" "blue" "8/12/55")
           (record "Kelliot" "Kris" "f" "red" "2/13/61")])
         )))
