(ns game-test
  (:require
   [clojure.test :refer :all]
   [debugger :refer (dbg)]
   [game :refer :all]))

(deftest test-initial-game-stack
  (let [game (new-game (sample-board))
        ms (game-moves game)]
    (is (= (list ms game) (game-stack game)))))

(deftest test-game-equals
  (let [game (new-game (sample-board))
        same-game (new-game (sample-board))]
    (is (= game game))
    (is (= game same-game))))

(deftest test-unit-of-work
  (let [game (new-game (sample-board))
        [m & rem :as ms] (game-moves game)]
    (testing "when multiple moves are left" 
      (let [gs (game-stack game)
            actual (unit-of-work gs)
            game' (advance-game game m)
            ms' (game-moves game')
            expected (list ms' game' rem game)]
        (is (= (dbg expected) (dbg actual)))))
    #_(testing "when multiple moves are left" 
      (with-redefs [moves ]
        (is (= (list ms game) (game-stack game)))))))
