(ns game2-test
  (:require
   [clojure.test :refer :all]
   [debugger :refer (dbg)]
   [game2 :refer :all]
   [clojure.tools.namespace.repl :refer [refresh]]))

(deftest test-board-equals
  (let [b (sample-board)
        same-b (sample-board)
        different-b (board [1 2])]
    (is (= b b))
    (is (= b same-b))
    (is (not (= b different-b)))))

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

(deftest test-move
  (let [b (board [0 1 2 3])
        expected (board [0 2 6])
        m (move [1 3 6])]
    (is (= expected (apply-move b m)))))
