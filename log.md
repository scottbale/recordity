## 12/5/20

Why doesn't `lein repl` work with `[nrepl "0.3.1"]`?

    $ lein repl :headless :port 2112
    Error loading nrepl.server: Could not locate nrepl/server__init.class, nrepl/server.clj or nrepl/server.cljc on classpath.
    Error loading cider.nrepl: Syntax error compiling at (cider/nrepl.clj:1:1).

`require` in repl

    (require '(clojure.tools [logging :as log]))
