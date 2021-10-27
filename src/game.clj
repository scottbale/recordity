(ns game
  "peg game

  Board looks like this:
          x
        x   x 
      x   x   x
    x   x   x   x
  x   x   x   x   x

  Represent board by boolean array of length 15. Board can be visualized by arranging indices like
  so:

              z |
                V

                0
              1   2
            3   4   5
          6   7   8   9
        10  11  12  13  14
  x ->                     <- y

  True/false represents whether a peg is in that slot or not. A valid move is for a peg to jump over
  another peg into an empty slot, and the jumped-over peg is removed from the board. Repeat until no
  more moves are possible. Score is number of remaining pegs. Goal is lowest score. Lowest possible
  score is 1.
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

(def game-moves (comp moves last :boards))

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

(def game-score (comp score last :boards))

(defn choose-move
  "Given a board and a sequence of moves, returns a move"
  [board moves]
  ;;(first moves)
  (nth moves (rand-int (count moves)))
  )

(defn advance-game
  "Apply the move to the current board of the game, returning a new game."
  [{:keys [boards moves] :as game} move]
  (let [b (last boards)]
    {:boards (conj boards (perform-move b move))
     :moves (conj moves move)}))

(defn play-game [game]
  (loop [g game
         safety-valve 15]
    (let [b (last (:boards g))
          mooves (moves b)]
      ;; (print-board b)
      ;; (println (count mooves) "moves, " safety-valve "tries left")
      (if (or (zero? safety-valve) (empty? mooves))
        g
        (let [m (choose-move b mooves)]
          (recur (advance-game g m)
                 (dec safety-valve)))))))

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

#_(defn gather-games
  "Sort (low to high score) and filter games (keeping up to max-score)"
  [max-score games]
  (->> games
       (filter (comp (partial >= max-score) game-score))
       (sort-by game-score)))

(defn game-stack
  "Given a game, return an initial game stack, which is a sequence of size two. The first item is a
  sequence of the remaining possible moves. The second item is the game."
  [{:keys [boards] :as game}]
  (list (-> boards first moves) game))

(defn unit-of-work
  "Take game stack, do one move (?), return new game stack.
  If there is a move 'm', make a new stack frame composed of game' (advancing current game) and
  moves' (calculate moves for game').
  TODO how to handle if there isn't a move"
  [[moves game & substack :as game-stack]]
  (let [[m & ms] moves]
    (if m
      (let [game' (advance-game game m)
            moves' (game-moves game')]
        (if (seq ms)
          (conj substack game ms game' moves')
          (conj substack game' moves')))
      substack)))

(defn work-to-completion
  "Take game stack, return game stack where top game is completed."
  [[moves game & substack :as game-stack]]
  #_(if (seq moves)
    (recur ))
  )

(defn play-games
  "Return a lazy sequence of completed games, starting with an initial board"
  [board]
  (let [game (new-game board)
        game-stack (game-stack game)]
    (letfn [(complete-seq [stack]
              (let [[_ g & rem] (work-to-completion stack)]
                (if (seq rem)
                  (concat (list g) (lazy-seq complete-seq rem))
                  (list g))))]
      (complete-seq game-stack))))

(defn test-lazyness [n]
  (letfn [(lazy-inc-range [i]
            (if (= (+ 5 n) i)
              (do 
                (print "blammo" i)
                (list i))
              (concat (list i) (lazy-seq (lazy-inc-range (inc i)))))
            )]
    (lazy-inc-range n)))


(comment
  (refresh)

  (take 3 (test-lazyness 12))

  (debug-game (play-game (new-game (sample-board))))

  (concat 
   (game-stack (new-game (sample-board)))
   (game-stack (new-game (sample-board))))


  (-> (concat 
       (game-stack (new-game (sample-board)))
       (game-stack (new-game (sample-board))))
      unit-of-work)



  ;; multiple games - sort games by final score
  #_(doseq [g (->> (comp play-game new-game sample-board)
                   (repeatedly 12)
                   (gather-games 3))]
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



  ;; testing laziness with filtering, sorting
  ;; Conclusion: have to take before sort!
  (let [s (concat (range 5) (lazy-seq (println "kaboom") [5]))]
    (->> s
         (filter even?)
         (take 3)
         (sort >)
         ;; (take 3)
         ))

  #_(let [foo []]
      (if-let [m (first foo)]
        m
        :nope))

  (Arrays/copyOf (boolean-array (map pos? (range 0 15))) 15)
  aset-boolean
  interleave
  and)
