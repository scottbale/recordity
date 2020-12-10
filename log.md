## 12/5/20

Why doesn't `lein repl` work with `[nrepl "0.3.1"]`?

    $ lein repl :headless :port 2112
    Error loading nrepl.server: Could not locate nrepl/server__init.class, nrepl/server.clj or nrepl/server.cljc on classpath.
    Error loading cider.nrepl: Syntax error compiling at (cider/nrepl.clj:1:1).

Answer: because leiningen includes `nrepl` (and `clojure-complete`) automatically (as dev dependencies?)

`require` in repl

    (require '(clojure.tools [logging :as log]))

## 12/6/20

Questions
* Can the record values have whitespace needing trimming? 
* Can the input format of DOB fields be assumed to be fixed?
* Sort order = natural?
* A single input file has only one type of record, and the delimiter is known ahead of time?
* Records are newline-delimited?
* What about this
> ...a command line app that takes as input a file...
> The input is 3 files, each containing records stored in a different format
  Should it take an argument which is the name of a file? List of N filenames? How should delimiter
  of each be indicated?
