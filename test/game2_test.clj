(ns game2-test
  (:require
   [clojure.test :refer :all]
   [debugger :refer (dbg)]
   [game2 :refer :all]))

(deftest test-board-equals
  (let [board (sample-board)
        same-board (sample-board)]
    (is (= board board))
    (is (= board same-board))))

(deftest test-board
  (let [b (sample-board)
        b2 (board [1 3 5])]
    (is (= (range 1 15) (pegs b)))
    (is (= [1 3 5] (pegs b2)))))

(deftest test-board-str
  (let [expected (str  "        .\n"
                       "      x   x\n"
                       "    x   x   x\n"
                       "  x   x   x   x\n"
                       "x   x   x   x   x")]
    (is (= expected (board-str (sample-board))))))
