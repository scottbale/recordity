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
