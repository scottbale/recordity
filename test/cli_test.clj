(ns cli-test
  (:use
   [clojure.test])
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [java-time :as jt]
   [cli :refer :all]
   [recordity :as r]))

(deftest test-main-help
  (is (= 0 (main "-h"))))

(deftest test-main-no-args
  (is (= 1 (main))))



(comment

  ;; burnt umber
  (def colors ["red" "green" "blue" "yellow" "black" "chartreuse" "magenta" "periwinkle" "taupe" "mauve" "puce" "beige"])

  (defn random-color [] (nth colors (rand-int (count colors))))
  
  (defn random-dob 
    "random between epoch and about 1996-02-04"
    []
    (let [millis 823456878993]
      (jt/local-date (jt/instant (long (rand millis))) 1)))

  ;; form test records, write records.edn
  (with-open [mnames (io/reader (io/resource "mnames.txt"))
              fnames (io/reader (io/resource "fnames.txt"))
              rs-edn (io/writer (io/resource "records.edn"))]

    (letfn [(from-raw [gender line]
              (apply record (concat (reverse (str/split line #"\t")) [gender (random-color) (random-dob)])))]
      (let [records (concat (map (partial from-raw "m") (line-seq mnames))
                            (map (partial from-raw "f") (line-seq fnames)))]
        (binding [*out* rs-edn]
          (pr records)))))

  ;; custom reader
  (defn read-local-date [[_ _ date-str]]
    (jt/local-date "yyyy-MM-dd" date-str))

  (edn/read-string {:readers {'object read-local-date}} 
                   "#object[java.time.LocalDate 0x3ddb3c0 \"1983-10-08\"]")

  ;; records can't be read with edn/read by default
  (edn/read-string {:readers {'object read-local-date}} 
                   "#recordity.Record{:lastname \"Dickens\", :firstname \"Sean\", :gender \"m\", :color \"magenta\", :dob #object[java.time.LocalDate 0x7220ceb3 \"1990-09-09\"]}")

  (binding [*data-readers* {'object read-local-date}]
    (read-string "#object[java.time.LocalDate 0x3ddb3c0 \"1983-10-08\"]")
    (read-string "#recordity.Record{:lastname \"Dickens\", :firstname \"Sean\", :gender \"m\", :color \"magenta\", :dob #object[java.time.LocalDate 0x7220ceb3 \"1990-09-09\"]}"))

  ;; read records, write three test files
  ;; had to manually strip double-tics from resulting files
  ;; also hand-added some "burnt umber" color values to all but the space-delimited text file
  (binding [*data-readers* {'object read-local-date}]
    (with-open [rcds-reader (io/reader (io/resource "records.edn"))
                comma-rcds (io/writer (io/resource "comma-delimited.txt"))
                space-rcds (io/writer (io/resource "space-delimited.txt"))
                pipe-rcds (io/writer (io/resource "pipe-delimited.txt"))]
      (let [rcds (read (java.io.PushbackReader. rcds-reader))
            rcds (shuffle rcds)
            rcds1 (take 2200 rcds)
            rcds2 (take 3300 (drop 2200 rcds))
            rcds3 (take 2500 (drop (+ 2200 3300) rcds))]
        (doseq [[rcds delim w] [[rcds1 "," comma-rcds] 
                                [rcds2 " " space-rcds] 
                                [rcds3 "|" pipe-rcds]]]
          (binding [*out* w]
            (doseq [r rcds]
              (prn (r/record-str delim r/input-date-format r))))))))

  ;; unused
  (with-open [rs-edn (io/reader (io/resource "records.edn"))]
    (let [rs (edn/read {:readers {'object read-local-date}} (java.io.PushbackReader. rs-edn))]
      (doseq [r (take 5 rs)]
        (println ">>>>" r ">>>" (type r)))))

  ;; proto-CLI output
  (with-open [r (io/reader (io/resource "comma-delimited.txt"))]
    (let [rcds (map (partial r/parse-record (r/delimiters :comma) r/input-date-format) (line-seq r))]
      (doseq [r (map r/record-str (take 25 (sort (r/comparators :dob) rcds)))]
        (println r))))

  )
