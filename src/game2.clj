(ns game2
  "peg game

  Board looks like this:
          x
        x   x
      x   x   x
    x   x   x   x
  x   x   x   x   x

  Represent board by lowest 15 bits of a java primitive short, which is 16 bits. Board can be
  visualized by arranging bit indices like so:

              z |
                V

                0
              1   2
            3   4   5
          6   7   8   9
        10  11  12  13  14
  x ->                     <- y

  Bit set at index represents whether a peg is in that slot or not.

  A valid move is for a peg to jump over another peg into an empty slot, and the jumped-over peg is
  removed from the board. Repeat until no more moves are possible. Score is number of remaining
  pegs. Goal is lowest score. Lowest possible score is 1.

  Represent a move as a pair of shorts to use in bitwise operations against the game board.

  "
  (:require
   [debugger :refer [dbg]]
   [clojure.string :as str]
   [clojure.tools.namespace.repl :refer [refresh]])
  )

(def full-board (short 32767))
(def empty-board (short 0))

(def axes
  ;; vector of vectors - each contained vector is three-to-five indices representing a row of at
  ;; least three contiguous spaces on the board that must be searched for possible moves. Refer to
  ;; diagram above.
  [[3 7 12] [1 4 8 13] [0 2 5 9 14]   ;; xs
   [12 7 3] [13 8 4 1] [14 9 5 2 0]   ;; xs reversed
   [5 8 12] [2 4 7 11] [0 1 3 6 10]   ;; ys
   [12 8 5] [11 7 4 2] [10 6 3 1 0]   ;; ys reversed
   [3 4 5] [6 7 8 9] [10 11 12 13 14] ;; zs
   [5 4 3] [9 8 7 6] [14 13 12 11 10] ;; zs reversed
   ])

(defn board-str [board]
  (let [zs [[0] [1 2] [3 4 5] [6 7 8 9] [10 11 12 13 14]]
        padding (constantly " ")]
    (letfn [(pegstr [i]
              (if (bit-test board i) "x" "."))
            (rowstrs [[indices pads]]
              (concat
               (repeatedly pads padding)
               (interpose "   " (map pegstr indices))))]
      (str/join (apply concat (interpose "\n" (map rowstrs (map vector zs [8 6 4 2 0]))))))))

(defn build-board [pegs]
  (reduce (comp short bit-set) empty-board pegs))

(defn sample-board []
  (build-board (range 1 15)))

(defn pegs [board]
  (filter (partial bit-test board) (range 15)))

(defn move [[moving-peg jumped-peg target-peg :as pegs]]
  ;; return a pair of shorts
  (list
   (short (bit-set empty-board target-peg))
   (reduce (comp short bit-clear) full-board (take 2 pegs))))

(defn apply-move [board [bits all-except-bits :as move]]
  (-> board
      (bit-or bits)
      (bit-and all-except-bits)
      short))

(defn move?
  "Given board and indices of three adjacent spots, it is a legal move?
   i - spot peg moves from
   j - spot of peg being jumped over
   k - empty spot peg moves to"
  [board i j k]
  (and
   (true? (bit-test board i))
   (true? (bit-test board j))
   (false? (bit-test board k))))

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
         (if (apply move? board ijk)
           (conj moves (move ijk))
           moves))))))

(defn moves
  "Given a board, return a sequence of all (zero or more) legal moves."
  [board]
  (mapcat (partial moves-along-indices board) axes))

(comment




  )
