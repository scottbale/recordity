(ns recordity-test
  (:use
   [clojure.test])
  (:require
   [clojure.tools.logging :as log]
   [recordity :refer :all]))

(deftest test-foo
  (is (= "bar" foo)))
