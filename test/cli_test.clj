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
  (with-out-str
    (is (= 0 (main "-h")))))

(deftest test-main-no-args
  (with-out-str
    (is (= 1 (main)))))

(deftest test-validate-input-file
  (testing "nonexistent"
    (is (nil? (validate-input-file "not-anything.txt"))))
  (testing "existing resource"
    (is (not (nil? (validate-input-file "pipe-delimited.txt")))))
  (testing "existing file"
    (is (not (nil? (validate-input-file "README.md"))))))

(deftest test-zip-opts
  (testing "happy path"
    (is (= [["comma.txt" ","] ["pipe.txt" "|"]] (zip-opts ["pipe.txt" "comma.txt"] ["|" ","]))))
  (testing "too few delimiters specified, defaults to pipe"
    (is (= [["comma.txt" ","] ["pipe.txt" "|"]] (zip-opts ["pipe.txt" "comma.txt"] [","]))))
  (testing "too many delimiters specified are ignored"
    (is (= [["comma.txt" ","] ["pipe.txt" "|"]] (zip-opts ["pipe.txt" "comma.txt"] [" " "|" ","])))))

(deftest test-records
  (testing "three files; sort by DOB"
    (let [expected [(r/record "Welch" "Michael" "m" "green" (jt/local-date 1970 1 1))
                    (r/record "White" "Boris" "m" "mauve" (jt/local-date 1970 1 3))
                    (r/record "James" "Austin" "m" "puce" (jt/local-date 1970 1 5))
                    (r/record "Watson" "Donna" "f" "green" (jt/local-date 1970 1 5))
                    (r/record "Hughes" "Penelope" "f" "puce" (jt/local-date 1970 1 7))]
          rcds (records [["comma-delimited.txt" ","]
                         ["pipe-delimited.txt" "|"]
                         ["space-delimited.txt" " "]] "D")]
      (is (= 8000 (count rcds)))
      (is (= expected (take 5 rcds)))))
  (testing "two files; sort by gender, name"
    (let [expected [(r/record "Abraham" "Jessica" "f" "red" (jt/local-date 1987 9 23))
                    (r/record "Abraham" "Ruth" "f" "chartreuse" (jt/local-date 1984 10 30))
                    (r/record "Abraham" "Ella" "f" "puce" (jt/local-date 1987 5 18))
                    (r/record "Abraham" "Donna" "f" "puce" (jt/local-date 1973 10 31))
                    (r/record "Abraham" "Hannah" "f" "red" (jt/local-date 1989 10 2))]
          rcds (records [["comma-delimited.txt" ","]
                         ["space-delimited.txt" " "]] "G")]
      (is (= 5500 (count rcds)))
      (is (= expected (take 5 rcds))))))

(deftest test-blarf
  (let [expected (str/join ["Young Steven m puce 10/5/1984\n"
                            "Young Warren m mauve 7/26/1988\n"
                            "Young Joanne f red 5/12/1977\n"
                            "Young Neil m mauve 10/19/1974\n"
                            "Young Karen f periwinkle 11/25/1990\n"])
        actual (with-out-str
                 (is (= 0 (blarf (take 5 (records [["comma-delimited.txt" ","]["pipe-delimited.txt" "|"]] "N"))))))]
    (is (= expected actual))))

(comment

  ;; Below is how I created most files in `test-data/`

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
  )
