Recordity
=========

Clojure coding exercise: parse, sort and display records.

Requires [Clojure CLI tools](https://clojure.org/guides/deps_and_cli) (or, alternatively, [Leiningen](https://github.com/technomancy/leiningen) version 2.9.x).

CLI
---

To see CLI usage:

    clojure -M:recordity -h

Output the pipe-delimited file of records (with pipe delimiter being the default, and default sorting)

    clojure -M:recordity -f pipe-delimited.txt

Combine multiple files and delimiters, specify sorting

    clojure -M:recordity -f pipe-delimited.txt -d pipe -f space-delimited.txt -d space -f comma-delimited.txt -d comma -s D

The three `foo-delimited.txt` files are included as sample resources in the `test-data` directory.
The CLI will search for each `-f` value first as a file with that name and path, then as a resource.

REST API
--------

Implemented using [Ring](https://github.com/ring-clojure/ring) and
[Compojure](https://github.com/weavejester/compojure). State is held in rudimentary sessions using
Ring's [session middleware](https://github.com/ring-clojure/ring/wiki/Sessions) and in-memory
session store, which is why a session identifier cookie is included in some of the sample `curl`
statements below.

Start up the API in a jetty server instance:

    clojure -M:recordity-server

Here are some sample `curl` commands.

Get current (empty) results

    curl -sS -X GET -i --header 'Accept: application/json' --header 'Content-Type: application/json' 'http://localhost:3000/records/birthdate'

POST some records (note the `set-cookie` in the first response, it is the session identifier, use it in subsequent requests)

    curl -sS -X POST -i --header 'Content-Type: application/x-www-form-urlencoded' 'http://localhost:3000/records' -d 'record=Gartner%2CFarnsworth%2Cm%2Cblue%2C1962%2F11%2F22&delimiter=%2C'
    curl -sS -X POST -i --header 'cookie: recordity=08130f6d-7b9c-4b0e-ad76-65eaf50314f6' --header 'Content-Type: application/x-www-form-urlencoded' 'http://localhost:3000/records' -d 'record=Smith%2CBubba%2Cm%2Cgreen%2C1965%2F1%2F12&delimiter=%2C'

Now see a non-empty response

    curl -sS -X GET -i --header 'cookie: recordity=08130f6d-7b9c-4b0e-ad76-65eaf50314f6' --header 'Accept: application/json' --header 'Content-Type: application/json' 'http://localhost:3000/records/birthdate'

tests
-----

Run unit tests (uses [Cognitect Labs test runner](https://github.com/cognitect-labs/test-runner).)

    clj -X:test

log
---

    tail -f log/debug.log

Leiningen
---------

Note: `lein` 2.9.x can be used to run any of the above commands, e.g.

    lein run -- -f pipe-delimited.txt
or

    lein run restful/-main

For development I used `lein repl` task with
[cider-nrepl](https://github.com/clojure-emacs/cider-nrepl) middleware, plus emacs with
[cider](https://github.com/clojure-emacs/cider).
