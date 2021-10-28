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

(deftest test-new-game-stack
  (let [b (sample-board)
        g (new-game b)
        ms (moves b)
        expected (map vector (repeatedly (constantly g)) ms)]
    (is (= expected (new-game-stack g)))))

(deftest test-build-game-stack
  (let [b1 (sample-board)
        [m1 :as moves1] (moves b1)
        g1 (new-game b1)
        b2 (apply-move b1 m1)
        moves2 (moves b2)
        g2 (build-game [b1 b2] [m1])
        gs1 (build-game-stack g1 moves1)
        gs2 (build-game-stack gs1 g2 moves2)
        expected (concat
                  (map vector (repeatedly (constantly g2)) moves2)
                  (map vector (repeatedly (constantly g1)) moves1))]
    (is (= expected gs2))))

(deftest test-unit-of-work
  (let [b1 (sample-board)
        [m1 & rem1 :as moves1] (moves b1)
        g1 (new-game b1)
        b2 (apply-move b1 m1)
        [m2 & rem2 :as moves2] (moves b2)
        g2 (build-game [b1 b2] [m1])
        gs1 (build-game-stack g1 rem1)
        gs2 (build-game-stack gs1 g2 moves2)]
    (testing "unit of work - happy path"
      (let [b3 (apply-move b2 m2)
            moves3 (moves b3)
            g3 (build-game [b1 b2 b3] [m1 m2])
            expected-stack (-> gs1
                               (build-game-stack g2 rem2)
                               (build-game-stack g3 moves3))]
        (is (= expected-stack (unit-of-work gs2)))))
    (testing "top of stack has exactly one move - new stack should omit that game"
      (let [gs2' (build-game-stack gs1 g2 [m2])
            b3 (apply-move b2 m2)
            moves3 (moves b3)
            g3 (build-game [b1 b2 b3] [m1 m2])
            ;; expected result omits gs2' entirely because the game at the top of that stack will have
            ;; been entirely traversed (all moves tried). This unit of work will try the last remaining
            ;; move.
            expected-stack (-> gs1 (build-game-stack g3 moves3))]
        (is (= expected-stack (unit-of-work gs2')))))
    ))

(deftest test-complete-game-seq
  ;; Testing progress
  ;; Provide two functions
  ;; advance-game-fn - takes a game and a move, returns a game
  ;; moves-fn - takes a game, returns a sequence of zero or more moves
  ;; This test completely fabricates games and moves
  (let [games [:g1 :g2 :g3 :g4]
        game-moves {:g1 [:mA :mB]
                    :g2 [:mC]
                    :g3 [:mE :mF :mI :mJ]
                    :g4 []
                    :g5 [:mG]
                    :g6 []
                    :g7 []
                    :g8 [:mH]
                    :g9 []
                    :g10 []}
        game-successor {[:g1 :mA] :g2
                        [:g1 :mB] :g8
                        [:g8 :mH] :g3
                        [:g2 :mC] :g4
                        [:g3 :mE] :g5
                        [:g3 :mF] :g7
                        [:g5 :mG] :g6
                        [:g3 :mI] :g9
                        [:g3 :mJ] :g10}
        game-fn (comp game-successor list)]
    ;;(is (= [:g4 :g6 :g7 :g9 :g10] (complete-game-seq game-fn game-moves (build-game-stack :g1 (:g1 game-moves)))))
    (is (= [:g4 :g6 :g7] (take 3 (complete-game-seq game-fn game-moves (build-game-stack :g1 (:g1 game-moves))))))
    ))


(comment

  (let [m-fn (comp (partial take 2) game-next-moves)]
    (doseq [g (take 3 (complete-game-seq advance-game m-fn (new-game-stack (new-game (sample-board)))))]
      (println ">>>>>>score:" (game-score g))
      (println (-> g :boards last board-str))))


)
