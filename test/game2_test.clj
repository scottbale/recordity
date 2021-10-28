(ns game2-test
  (:require
   [clojure.test :refer :all]
   [debugger :refer (dbg)]
   [game2 :refer :all]
   [clojure.tools.namespace.repl :refer [refresh]]))

(deftest test-board-equals
  (let [b (sample-board)
        same-b (sample-board)
        different-b (build-board [1 2])]
    (is (= b b))
    (is (= b same-b))
    (is (not (= b different-b)))))

(deftest test-board
  (let [b (sample-board)
        b2 (build-board [1 3 5])]
    (is (= (range 1 15) (pegs b)))
    (is (= [1 3 5] (pegs b2)))))

(deftest test-board-score
  (let [b1 (build-board [13])
        b2 (build-board [1 3 5])]
    (is (= 1 (board-score b1)))
    (is (= 3 (board-score b2)))))

(deftest test-board-str
  (let [expected (str  "        .\n"
                       "      x   x\n"
                       "    x   x   x\n"
                       "  x   x   x   x\n"
                       "x   x   x   x   x")]
    (is (= expected (board-str (sample-board))))))

(deftest test-move
  (let [b (build-board [0 1 2 3])
        expected (build-board [0 2 6])
        m (build-move [1 3 6])]
    (is (= expected (apply-move b m)))))

(deftest test-move?
  (let [b (build-board [3 5 6 7])]
    (is (move? b 3 6 10))
    (is (move? b 6 3 1))
    (is (move? b 3 7 12))
    (is (move? b 6 7 8))
    (is (not (move? b 10 6 3)))
    (is (not (move? b 2 5 9)))
    (is (not (move? b 12 13 14)))))

(deftest test-all-moves
  (let [b-one-move (build-board [0 1])
        b-no-moves (build-board [0 7 13])
        one-move (list (build-move [0 1 3]))]
    (is (= one-move (moves b-one-move)))
    (is (empty? (moves b-no-moves)))))

(deftest test-new-game
  (let [board (sample-board)
        game (new-game board)]
    (is (empty? (game-moves game)))
    (is (= [board] (game-boards game)))))

(deftest test-build-game
  (let [[m1 m2 :as moves] [(build-move [5 2 0])
                           (build-move [7 4 2])]
        boards [(sample-board)
                (-> (sample-board) (apply-move m1))
                (-> (sample-board) (apply-move m1) (apply-move m2))]
        game (build-game boards moves)]
    (is (= boards (game-boards game)))
    (is (= moves (game-moves game)))))

(deftest test-advance-game
  (let [b1 (sample-board)
        m1 (first (moves b1))
        b2 (apply-move b1 m1)
        m2 (first (moves b2))
        game (build-game [b1 b2] [m1])
        b3 (apply-move b2 m2)
        expected (build-game [b1 b2 b3] [m1 m2])]
    (is (= expected (advance-game game m2)))))
