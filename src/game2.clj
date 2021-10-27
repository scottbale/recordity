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

(comment




  )
