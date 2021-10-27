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
  "
  (:require
   [debugger :refer [dbg]]
   [clojure.string :as str]
   [clojure.tools.namespace.repl :refer [refresh]])
  )

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

(defn board [pegs]
  (let [bits (short 0)]
    #_(reduce (fn [val p]
              (short (bit-set val p))) (short 0) pegs)
    (reduce (comp short bit-set) (short 0) pegs)))

(defn sample-board []
  (board (range 1 15)))

(defn pegs [board]
  (filter (partial bit-test board) (range 15)))


(comment




  )
