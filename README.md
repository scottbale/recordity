Recordity
=========

Clojure coding exercise: parse, sort and display records.

To see CLI usage:

```
lein run -- -h
```

Output the pipe-delimited file of records (with pipe delimiter being the default, and default sorting)

```
lein run -- -f pipe-delimited.txt
```

Combine multiple files and delimiters, specify sorting

```
lein run -- -f pipe-delimited.txt -d "|" -f space-delimited.txt -d " " -f comma-delimited.txt -d "," -s D
```
