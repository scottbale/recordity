(ns bank
  "https://codingdojo.org/kata/BankOCR/"
  (:require
   [clojure.java.io :as io]))


(def test1
  [" _  _  _  _  _  _  _  _  _ "
   "| || || || || || || || || |"
   "|_||_||_||_||_||_||_||_||_|"])

(def testN
  ["    _  _     _  _  _  _  _ " 
   "  | _| _||_||_ |_   ||_||_|" 
   "  ||_  _|  | _||_|  ||_| _|"])

(defn blocks 
  "Transforms a sequence of three raw input lines (encoding a nine-digit account number) into a
  sequence of vectors, each vector containing three strings which combine (stack) to form a single
  digit."
  [lines]
  (let [xs (map (fn [line] 
                  (->> line
                       (partition-all 3)
                       (map #(apply str %1)))) lines)]
    (apply map vector xs)))

(def digits {[" _ " "| |" "|_|"] 0
             ["   " "  |" "  |"] 1
             [" _ " " _|" "|_ "] 2
             [" _ " " _|" " _|"] 3 
             ["   " "|_|" "  |"] 4 
             [" _ " "|_ " " _|"] 5 
             [" _ " "|_ " "|_|"] 6 
             [" _ " "  |" "  |"] 7 
             [" _ " "|_|" "|_|"] 8 
             [" _ " "|_|" " _|"] 9})

(defn record-seq
  "Sequence of lines from a file, eagerly loaded into memory."
  [f]
  (with-open [r (io/reader (io/file f))]
    (doall (line-seq r))))

(defn read-account-number 
  "Given the sequence of three strings which, stacked, form a 9-digit account number, return the
  number."
  [lines]
  (map digits (blocks lines)))

(comment

  (map read-example (partition-all 3 4 (record-seq "digits.txt")))

  (blocks test1)

  (blocks testN)

  (read-account-number testN) ;; (1 2 3 4 5 6 7 8 9)

  )
