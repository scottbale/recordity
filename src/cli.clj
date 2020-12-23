(ns cli
  "command-line interface to recordity"
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [clojure.tools.cli :refer [parse-opts]]
   [recordity :as r])
  (:gen-class))

(def sorts->comparators {:G (r/comparators :genderThenLastName)
                         :N (r/comparators :lastNameDesc)
                         :D (r/comparators :dob)})

(def delims {:pipe (r/delimiters :pipe)
             :comma (r/delimiters :comma)
             :space (r/delimiters :space)})

(defn mconj
  "In associate structure `m`, conj `x` onto collection value at key `id`. Used to build up a sequence
  of one or more CLI options. Depends specifically on `nil` being the initial value, so that the
  resulting sequence is a list with the user's final option at the head (as `conj`ing on a list
  prepends additional values to the head of the list.) See also `zip-opts`."
  [m id x]
  (update m id conj x))

(defn validate-input-file
  "Validate function for file CLI option: String param `f` must be an existing resource or
  java.io.File. Return nil for nonexistent (fails validation) or a non-nil value to pass
  validation."
  [^String f]
  (or
   (.exists (io/file f))
   (io/resource f)))

(def cli-options
  [["-f" "--file filename" "A file of records. Each record must be newline-delimited."
    :assoc-fn mconj
    :validate [validate-input-file "file does not exist."]]
   ["-d" "--delim delimiter" "Indicates character that delimits the fields of each record. Use one of 'pipe', 'comma', or 'space' to indicate which one. Any file(s) with unspecified delimiter(s) will default to pipe."
    :assoc-fn mconj
    :parse-fn (comp keyword string/lower-case)
    :validate-fn [(set (keys delims))]]
   ["-s" "--sorting sort-option" "Sort the output in one of three ways: 'G' to sort by gender (females first), then last name ascending; 'D' to sort by DOB ascending; 'N' (default) to sort by last name descending."
    :default :N
    :parse-fn keyword
    :validate-fn [(set (keys sorts->comparators))]]
   ["-h" "--help"]])

(defn usage [summary]
  (->> ["Load, sort and display records."
        ""
        "Usage: recordity [options]"
        ""
        "Options:"
        summary
        ""
        "Example: recordity -f records.txt -d pipe -f more.txt -d comma -s D"
        ""]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options)
      {:exit-message (usage summary) :ok? true}

      errors
      {:exit-message (error-msg errors) :ok? false}

      (:file options)
      options

      :else
      {:exit-message (usage summary)})))

(defn exit-msg [status message]
  (println message)
  (log/log (if (zero? status) :info :error)
           message)
  status)

(defn exit-err [e]
  (let [message (format "Caught %s: %s. See log file for details." (.. e getClass getSimpleName) (.getMessage e))
        status 1]
    (println message)
    (log/error e message)
    status))

(defn zip-opts
  "Returns alist sequence of filename to delimiter. Excess delimiters are ignored. Default delimiter
  is used if too few are supplied. There's a subtle dependency on `mconj` here: The two parameters
  result from `mconj`, meaning they are lists in reverse order of options as supplied by user via
  CLI. So any filenames lacking explicitly-supplied delimiters (relying on default) are at the head
  of the list. Therefore, reverse both list params. This seems brittle and subtle, room for
  improvement, TODO. (This is what I get for being clever: implementing CLI to allow mismatched
  filename and delim options, which was not required.)"
  [filenames delimiters]
  (map vector (reverse filenames) (concat (reverse delimiters) (repeat :pipe))))

(defn record-seq
  "Sequence of records from a single file and delimiter pair, eagerly loaded entirely into memory."
  [[f delim]]
  (with-open [r (io/reader (or (io/resource f) (io/file f)))]
    (doall (map (partial r/parse-record (delims delim) r/input-date-format) (line-seq r)))))

(defn records
  "For the one or more filename/delimiter pair(s), return a single sequence (concatenation of all the
  sequence(s) from individual file(s)). Returned sequence is sorted."
  [files-alist sorting]
  (sort (sorts->comparators sorting) (mapcat record-seq files-alist)))

(defn blarf
  "Print sorted records."
  [rcds]
  (doseq [rcd rcds]
    (println (r/record-str rcd)))
  0)

(defn main [& args]
  (log/info "Starting Recordity CLI with args" args)
  (try
    (let [{:keys [file delim sorting exit-message ok?] :as opts} (validate-args args)]
      (if file
        (let [zipopts (zip-opts file delim)]
          (log/debug "CLI options:" opts "zipped options:" zipopts "sorting:" sorting)
          (blarf (records zipopts sorting)))
        (exit-msg (if ok? 0 1) exit-message)))
    (catch Exception e
      (exit-err e))))

(defn exit [status]
  (System/exit status))

(defn -main [& args]
  (exit (apply main args)))
