(ns game
  "peg game

  Board looks like this:
          x
        x   x 
      x   x   x
    x   x   x   x
  x   x   x   x   x

  Represent board by arrangement of indices of boolean array of length 15:

              z |
                V

                0
              1   2
            3   4   5
          6   7   8   9
        10  11  12  13  14
  x ->                     <- y

  Goal is to get down to one peg.
  "
  (:require
   [debugger :refer [dbg]]
   [clojure.tools.namespace.repl :refer [refresh refresh-all]])
  (:import
   [java.util Arrays]))

(defn sample-board []
  {:pegs (boolean-array (map pos? (range 0 15)))})

(defn new-game [first-board]
  {:boards [first-board] :moves []})

(def zs [[0] [1 2] [3 4 5] [6 7 8 9] [10 11 12 13 14]])

(def axes
  ;; vector of vector - each contained vector is a vector of three-to-five indices representing a
  ;; row of contiguous spaces on the board that must be searched for legal moves
  [[3 7 12] [1 4 8 13] [0 2 5 9 14]   ;; xs
   [12 7 3] [13 8 4 1] [14 9 5 2 0]   ;; xs reversed
   [5 8 12] [2 4 7 11] [0 1 3 6 10]   ;; ys
   [12 8 5] [11 7 4 2] [10 6 3 1 0]   ;; ys reversed
   [3 4 5] [6 7 8 9] [10 11 12 13 14] ;; zs
   [5 4 3] [9 8 7 6] [14 13 12 11 10] ;; zs reversed
   ])


(defn perform-move
  "Given a board and a move, return a new board representing the move applied to the given board"
  [board move]
  (let [pegs-copy (Arrays/copyOf (:pegs board) 15)
        [i j k] (:indices move)]
    (aset-boolean pegs-copy i false)
    (aset-boolean pegs-copy j false)
    (aset-boolean pegs-copy k true)
    {:pegs pegs-copy}))

(defn move?
  "Given board and indices of three adjacent spots, is it a legal move?" 
  [{:keys [pegs]} [i j k]]
  (and 
   (true? (aget pegs i))
   (true? (aget pegs j))
   (false? (aget pegs k))))

(defn moves-along-indices
  "Given a board and three to five indices representing adjacent spots on the board, calculate legal
  moves"
  [board indices]
  (loop [examine indices
         moves []]
    (let [ijk (take 3 examine)]
      (if (< (count ijk) 3)
        moves
        (recur 
         (rest examine)
         (if (move? board ijk)
           (conj moves {:indices ijk})
           moves))))))

(defn moves
  "Given a board, return a sequence of all (zero or more) legal moves."
  [board]
  (mapcat (partial moves-along-indices board) axes))

(defn print-board [board]
  (let [pegs (:pegs board)]
    (print "\n")
    (doseq [[indices pad] (map vector zs [8 6 4 2 0])]
      (dotimes [_ pad] (print " "))
      (doseq [i indices]
        (if (true? (aget pegs i))
          (print "x   ")
          (print ".   ")))
      (print "\n")))
  board)

(defn score
  "Number of pegs left. Less is better."
  [{:keys [pegs] :as board}]
  (count (filter true? (seq pegs))))

(defn choose-move
  "Given a board and a sequence of moves, returns a move"
  [board moves]
  ;;(first moves)
  (nth moves (rand-int (count moves)))
  )

(defn play-game [game]
  (let [board (first (:boards game))]
    (loop [g game
           b board
           safety-valve 15]
      (let [mooves (moves b)]
        ;; (print-board b)
        ;; (println (count mooves) "moves, " safety-valve "tries left")
        (if (or (zero? safety-valve) (empty? mooves))
          g
          (let [m (choose-move b mooves)
                b' (perform-move b m)
                bs (:boards g)
                ms (:moves g)]
            (recur {:boards (conj bs b') :moves (conj ms m)}
                   b'
                   (dec safety-valve))))))))

(defn print-move [move]
  (let [[i j k] (:indices move)]
    (println "\nmove:" i j k)))

(defn debug-game [{:keys [boards moves] :as game}]
  (doseq [[b m] (map vector boards moves)]
    (print-board b)
    (print-move m))
  (let [final-board (last boards)]
    (print-board final-board)
    (println "\nscore:" (score final-board))))

(defn print-game [{:keys [boards] :as game}]
  (let [final-board (last boards)]
    (print-board final-board)
    (println "\nscore:" (score final-board))))

(comment
  (refresh)
  (debug-game (play-game (new-game (sample-board))))

  ;; multiple games - sort games by final score
  (doseq [g (sort-by (comp score last :boards) (repeatedly 12 (comp play-game new-game sample-board)))]
    (println ">>>>>>>>")
    (print-game g))

  (moves (sample-board))
  (move? (sample-board) [5 2 0])
  (move? (sample-board) [3 1 0])
  (move? (sample-board) [9 5 2])
  (moves-along-indices (sample-board) [14 9 5 2 0])
  (moves-along-indices (sample-board) [9 5 2])
  (print-board (sample-board))
  (print-board (perform-move (sample-board) {:indices [5 2 0]}))
  (moves (perform-move (sample-board) {:indices [5 2 0]}))

  (Arrays/copyOf (boolean-array (map pos? (range 0 15))) 15)
  aset-boolean
  interleave
  and
  )
