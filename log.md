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

## 12/10/20

Added `clojure.java-time` dep, using for dates

## 12/12/20

Questions
* Delimiters in data - can I assume none of the three types of delimiters
  are in _any_ record type? If not, then that implies the record format might need to keep track of
  the delimiter it was parsed with for possible round-tripping. I.e. if I to-string a record back to
  a delimited string, from a mixed set of records, how best to do that?
  * Output as .edn
  * Output with well-known prefixes (like a map tostring)
  * User can supply output delimiter via CLI

Generated fake names at https://homepage.net/name_generator/

Made test data files in `test-data/`, generated using some scratch work in commented section of
`cli_test.clj`. Just getting started on `cli.clj`.

CLI - able to parse multiple filename and multiple delimiter options

## 12/13/20

Done with CLI.

Ran into an interesting thing where a LazySeq passed to `log/debugf` produced a toString of
`LazySeq`, whereas passing it to `log/debug` did the desired thing. See also
https://stackoverflow.com/a/23407403/2495576

## 12/14/20

Test CLI w/ `lein run`, e.g.

    lein run -- -f pipe-delimited.txt

Starting REST API

## 12/16/20

update README

## 12/17/20

`restful` namespace and tests

example url-encoded body string:

    "record=Barkley%2CFarnsworth%2Cm%2Cbrownish%2C1962%2F11%2F22&delimiter=%2C"

example curl statements:

    $ curl -sS -X POST -i --header 'Content-Type: application/x-www-form-urlencoded' 'http://localhost:3000/records' -d 'record=Gartner%2CFarnsworth%2Cm%2Cblue%2C1962%2F11%2F22&delimiter=%2C'
    HTTP/1.1 204 No Content
    Date: Fri, 18 Dec 2020 03:51:13 GMT
    Set-Cookie: ring-session=acf8a055-3e7b-41ad-8c91-bb9049ad154f;Path=/;HttpOnly
    Server: Jetty(9.4.31.v20200723)

    $ curl -sS -X GET -i --header 'Accept: application/json' --header 'Content-Type: application/json' 'http://localhost:3000/records/gender'
    HTTP/1.1 200 OK
    Date: Fri, 18 Dec 2020 03:09:06 GMT
    Content-Type: application/json
    Content-Length: 2
    Server: Jetty(9.4.31.v20200723)

Still need to figure out
* stopping/starting jetty server
* cookies/session keys via curl

## 12/18/20

starting jetty server - needed to use `:join? false` option
cookies/session - needed to stringify UUID session key
Scratch work in comment block of `restful-test`

Example curl statements:

    curl -sS -X GET -i --header 'Accept: application/json' --header 'Content-Type: application/json' --header 'cookie: recordity=6e144ccf-648f-42d3-ac30-6fafc871c7bc' 'http://localhost:3000/records/gender'
    curl -sS -X POST -i --header 'Content-Type: application/x-www-form-urlencoded' --header 'cookie: recordity=6e144ccf-648f-42d3-ac30-6fafc871c7bc' 'http://localhost:3000/records' -d 'record=Gartner%2CFarnsworth%2Cm%2Cblue%2C1962%2F11%2F22&delimiter=%2C'

(To pretty-print json response, omit the `-i` and pipe to `json_pp`.)

run API with lein:

    lein run restful/-main

Getting some console logging from Jetty - probably it uses logback instead of log4j2?

## 12/20/20

Figuring out how to launch w/ `clojure` and `deps.edn` instead of `lein` and `project.clj`:

CLI

    clojure -M -m cli -h

REST API

    clojure -M -m restful

Updated README

Would be nice to add some additional logging to `restful.clj`, perhaps middleware that logs basic
stats about the session (session id, count of records) for each request.

## 12/22/20

Rejiggered the CLI to use require strings "comma" etc. in place of the actual delimiter symbols.
Mainly because space was a problem otherwise. Added some unit tests.

Asked in Clojurians slack about how one makes an idiomatic CLI in Clojure these days. Got a variety
of helpful answers:
* in deps.edn, add an alias with `:main-opts` (this is what I went with)
* cli-matic
* babashka
* docopt
* plain old .clj file as a script that can be invoked directly

Switched from `log4j2` only to `slf4j` with `log4j2` binding, allowed me to capture (and quiet down)
jetty logging.

## Appendix - links

* compojure
  * https://github.com/weavejester/compojure
  * wiki https://github.com/weavejester/compojure/wiki/Destructuring-Syntax
  * API https://weavejester.github.io/compojure/compojure.response.html
* ring
  * https://github.com/ring-clojure/ring
  * wiki https://github.com/ring-clojure/ring/wiki/Sessions
  * API https://ring-clojure.github.io/ring/ring.util.request.html
* ring-mock https://github.com/ring-clojure/ring-mock
* ring-defaults https://github.com/ring-clojure/ring-defaults
* cheshire https://github.com/dakrone/cheshire
