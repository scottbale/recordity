(ns bank
  "https://codingdojo.org/kata/BankOCR/"
  (:require
   [clojure.java.io :as io]
   [debugger :refer [dbg]]))


(def test1
  [" _  _  _  _  _  _  _  _  _ "
   "| || || || || || || || || |"
   "|_||_||_||_||_||_||_||_||_|"
   "                           "])

(def testN
  ["    _  _     _  _  _  _  _ " 
   "  | _| _||_||_ |_   ||_||_|" 
   "  ||_  _|  | _||_|  ||_| _|"
   "                           "])

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

(defn runner
  "Input is a sequence of strings representing one or more 9-digit numbers"
  [input]
  (let [all-raws (partition-all 3 4 input)
        ;; each is a list of nine triples, each triple is the three strings that, stacked, form one of the nine digits
        acct-numbers-triples (map blocks all-raws)]
    (map
     (fn [acct-number-triples]
       (map digits acct-number-triples))
     acct-numbers-triples)))

(comment

  (runner testN)
  (runner (concat test1 testN))
  ;; note last acct number in this file appears malformed
  (runner (record-seq "digits.txt"))

  (blocks test1)
  (blocks testN)

  (read-account-number testN) ;; (1 2 3 4 5 6 7 8 9)

  )
