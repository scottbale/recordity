(ns game
  "peg game

  Board looks like this:
        x
      x   x 
    x   x   x
  x   x   x   x
x   x   x   x   x

Represent board by arrangement of indices of boolean array of length 15:

        0
      1   2
    3   4   5
  6   7   8   9
10  11  12  13  14

  Goal is to get down to one peg.
  "
  (:require
   [debugger :refer [dbg]]))

(defn sample-game []
  {:pegs (boolean-array (map pos? (range 0 15)))})

;; (def xs [[10] [6 11] [3 7 12] [1 4 8 13] [0 2 5 9 14]])
;; (def ys [[14] [9 13] [5 8 12] [2 4 7 11] [0 1 3 6 10]])
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


(defn perform-move [game move]
  (let [pegs-copy (java.util.Arrays/copyOf (:pegs game) 15)
        [i j k] (:indices move)]
    (aset-boolean pegs-copy i false)
    (aset-boolean pegs-copy j false)
    (aset-boolean pegs-copy k true)
    {:pegs pegs-copy}))

(defn move?
  "Given game and indices of three adjacent spots, is it a legal move?" 
  [{:keys [pegs]} [i j k]]
  (and 
   (true? (aget pegs i))
   (true? (aget pegs j))
   (false? (aget pegs k))))

(defn moves-along-indices
  "Given three to five indices representing adjacent spots on the board, calculate legal moves"
  [game indices]
  (loop [examine indices
         moves []]
    (let [ijk (take 3 examine)]
      (if (< (count ijk) 3)
        moves
        (recur 
         (rest examine)
         (if (move? game ijk)
           (conj moves {:indices ijk})
           moves))))))

(defn moves [game]
  (mapcat (partial moves-along-indices game) axes))

(defn print-game [game]
  (let [pegs (:pegs game)]
    (doseq [[indices pad] (map vector zs [8 6 4 2 0])]
      (dotimes [_ pad] (print " "))
      (doseq [i indices]
        (if (true? (aget pegs i))
          (print "x   ")
          (print ".   ")))
      (print "\n")))
  game)

(defn score
  "Number of pegs left. Less is better."
  [{:keys [pegs] :as game}]
  (count (filter true? (seq pegs))))

(defn choose-move [game moves]
  (first moves))

(defn play-game [game]
  (loop [g game
         safety-valve 15]
    (let [mooves (moves g)]
      ;; (print-game g)
      ;; (println (count mooves) "moves, " safety-valve "tries left")
      (if (or (zero? safety-valve) (empty? mooves))
        g
        (recur (perform-move g (choose-move g mooves))
               (dec safety-valve))))))

(defn debug-game [game]
  (print-game game)
  (println "score: " (score game)))

(comment
  (debug-game (play-game (sample-game)))
  (moves (sample-game))
  (move? (sample-game) [5 2 0])
  (move? (sample-game) [3 1 0])
  (move? (sample-game) [9 5 2])
  (moves-along-indices (sample-game) [14 9 5 2 0])
  (moves-along-indices (sample-game) [9 5 2])
  (print-game (sample-game))
  (print-game (perform-move (sample-game) {:indices [5 2 0]}))
  (moves (perform-move (sample-game) {:indices [5 2 0]}))

  (java.util.Arrays/copyOf (boolean-array (map pos? (range 0 15))) 15)
  aset-boolean
  interleave
  )
