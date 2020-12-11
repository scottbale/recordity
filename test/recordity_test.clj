(ns recordity-test
  (:use
   [clojure.test])
  (:require
   [clojure.tools.logging :as log]
   [java-time :as jt]
   [recordity :refer :all]))

(deftest test-parse-pipe-delimited
  (is (= (record "Smith" "John" "m" "green" (jt/local-date 1953 1 2))
         (parse-record (delimiters :pipe) input-date-format "Smith|John|m|green|1953/1/2"))))

(deftest test-parse-comma-delimited
  (is (= (record "Smith" "John" "m" "green" (jt/local-date 1953 1 2))
         (parse-record (delimiters :comma) input-date-format "Smith,John,m,green,1953/1/2"))))

(deftest test-parse-space-delimited
  (is (= (record "Smith" "John" "m" "green" (jt/local-date 1953 1 2))
         (parse-record (delimiters :space) input-date-format "Smith John m green 1953/1/2"))))

(deftest test-parse-date
  (is (= (jt/local-date 1953 1 2) (parse-date "1953/1/2")))
  (is (= (jt/local-date 1953 1 2) (parse-date canonical-date-format "1/2/1953"))))

(deftest test-sort-by-gender-then-last-name
  (is (= [
          (record "Kelliot" "Kris" "f" "red" (jt/local-date 1961 2 13))
          (record "Jabar" "Aaron" "m" "blue" (jt/local-date 1955 8 12))
          (record "Smith" "John" "m" "green" (jt/local-date 1953 1 2))
          ]

         (sort
          (comparators :genderThenLastName)
          [(record "Smith" "John" "m" "green" (jt/local-date 1953 1 2))
           (record "Kelliot" "Kris" "f" "red" (jt/local-date 1961 2 13))
           (record "Jabar" "Aaron" "m" "blue" (jt/local-date 1955 8 12))])
         )))

(deftest test-sort-by-last-name-desc
  (is (= [
          (record "Smith" "John" "m" "green" (jt/local-date 1953 1 2))
          (record "Kelliot" "Kris" "f" "red" (jt/local-date 1961 2 13))
          (record "Jabar" "Aaron" "m" "blue" (jt/local-date 1955 8 12))
          ]

         (sort
          (comparators :lastNameDesc)
          [(record "Smith" "John" "m" "green" (jt/local-date 1953 1 2))
           (record "Jabar" "Aaron" "m" "blue" (jt/local-date 1955 8 12))
           (record "Kelliot" "Kris" "f" "red" (jt/local-date 1961 2 13))])
         )))
