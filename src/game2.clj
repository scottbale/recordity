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

  Bit set at index represents whether a peg is in that spot or not.

  A valid move is for a peg to jump over another peg into an empty spot, and the jumped-over peg is
  removed from the board. Repeat until no more moves are possible. Score is number of remaining
  pegs. Goal is lowest score. Lowest possible score is 1.

  Represent a move as a pair of shorts to use in bitwise operations against the game board.
  "
  (:require
   [debugger :refer [dbg]]
   [clojure.tools.namespace.repl :refer [refresh]]))

(def full-board (short 32767))
(def empty-board (short 0))

(def ^:private axes
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

(def ^:private board-str-indices [[0] [1 2] [3 4 5] [6 7 8 9] [10 11 12 13 14]])

(defn board-str
  "Return a string representation of the board, similar to the diagram at the top of this namespace."
  [board]
  (let [padding (constantly " ")]
    (letfn [(pegstr [i]
              (if (bit-test board i) "x" "."))
            (rowstrs [[indices pads]]
              (concat
               (repeatedly pads padding)
               (interpose "   " (map pegstr indices))))]
      (->> (map vector board-str-indices [8 6 4 2 0])
           (map rowstrs)
           (interpose "\n")
           (apply concat)
           (apply str)))))

(defn build-board
  "Build a board, accepting a sequence of indices in the range of 0 to 14, each representing a spot
  on the board for a peg to be located."
  [pegs]
  (reduce (comp short bit-set) empty-board pegs))

(defn sample-board []
  (build-board (range 1 15)))

(defn pegs
  "Return a sequence of longs representing the indices of the pegs on the board."
  [board]
  (filter (partial bit-test board) (range 15)))

(def board-score (comp count pegs))

(defn build-move
  "Build a move, specifying the indices of the three pegs in order: the peg to do the 'jumping', the
  peg to be jumped (and removed), and the empty spot for the first peg to land in."
  [[moving-peg jumped-peg target-peg :as pegs]]
  ;; return a pair of shorts
  ;; The first short is the single bit representing the target peg
  ;; The second short is the two bits to be cleared on the board, representing the
  ;; two pegs being removed from the board
  (list
   (short (bit-set empty-board target-peg))
   (reduce (comp short bit-clear) full-board (take 2 pegs))))

(defn apply-move
  "Given a board and a move, apply the move to the board, returning a new resulting board."
  [board [set-bit unset-bits :as move]]
  ;; Do bitwise operations with the two shorts representing the move
  ;; The first short is a single bit to be set, representing the target peg. So do a
  ;; bitwise-or with the game board.
  ;; The second short is the two bits being unset, representing the other two pegs in
  ;; the move, whose spots on the board are being vacated. Unset those bits on the
  ;; board by doing a bitwise-and.
  (-> board
      (bit-or set-bit)
      (bit-and unset-bits)
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
           (conj moves (build-move ijk))
           moves))))))

(defn moves
  "Given a board, return a sequence of all (zero or more) legal moves."
  [board]
  (mapcat (partial moves-along-indices board) axes))

(defn build-game
  "A game is a history of boards and moves between boards. A game is complete if the final board has
  zero moves."
  [boards moves]
  {:boards boards
   :moves moves})

(defn new-game
  [board]
  (build-game [board] []))

(def game-next-moves (comp moves last :boards))

(def game-score (comp board-score last :boards))

(defn game-moves
  "The history of moves in the game. Returns a sequence of zero or move moves."
  [game] (:moves game))

(defn game-boards
  "The history of boards in the game. Returns a sequence of one or more boards."
  [game] (:boards game))

(defn advance-game
  "Given a game and a move to be performed, perform the move and return a new game representing the
  new state of the game"
  [{:keys [boards moves] :as game} move]
  (let [b (last boards)]
    (build-game
     (conj boards (apply-move b move))
     (conj moves move))))

(defn build-game-stack
  "A game stack is a sequence of pairs. Each pair is a game in-progress and a move to be attempted. So
  the same game may be duplicated in the stack, paired with different moves."
  ([game next-moves]
   (map vector (repeatedly (constantly game)) next-moves))
  ([game-stack game next-moves]
   (concat (build-game-stack game next-moves) game-stack)))

(defn new-game-stack
  "Given a game, return an initial game stack"
  [{:keys [boards] :as game}]
  (build-game-stack game (-> boards first moves)))

(defn unit-of-work
  "Given a game stack, perform a unit of work and return a new game stack."
  [[[game move] & rem-stack :as game-stack]]
  (let [game' (advance-game game move)
        moves' (game-next-moves game')]
    (build-game-stack rem-stack game' moves')))

(defn complete-game-seq
  [advance-game-fn game-moves-fn game-stack]
  ;; (println ">>>>>>" game-stack)
  (letfn [(step [[[game move] & rem-stack :as gs] safety-valve]
            ;; (println ">>>>>>" safety-valve game move gs)
            (if (or (== 0 safety-valve) (nil? game) (nil? move))
              nil
              (let [game' (advance-game-fn game move)
                    moves' (game-moves-fn game')]
                (if-not (seq moves')
                  (concat [game'] (lazy-seq (step rem-stack (dec safety-valve))))
                  (recur (build-game-stack rem-stack game' moves') (dec safety-valve))))))]
    (step game-stack 15)))


(comment

  (let [b (sample-board)
        g (new-game b)
        m (rand-nth (moves b))
        {:keys [boards] :as g} (advance-game g m)
        m (rand-nth (moves (last boards)))
        {:keys [boards] :as g} (advance-game g m)
        m (rand-nth (moves (last boards)))
        {:keys [boards] :as g} (advance-game g m)
        m (rand-nth (moves (last boards)))
        {:keys [boards] :as g} (advance-game g m)
        m (rand-nth (moves (last boards)))
        {:keys [boards]} (advance-game g m)
        ]
    (println (board-str (last boards))))

)
