(ns cli
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(defn mconj [m id x]
  (update m id conj x))

(def cli-options
  [["-f" "--file filename" "A file of records. Each record must be newline-delimited."
    :assoc-fn mconj
    :validate [(comp (memfn exists) io/file)]]
   ["-d" "--delim delimiter" "Delimiter character that delimits the fields of each record. Must be one of '|' (pipe), ',' (comma), or space."
    :assoc-fn mconj
    :validate [#{"|" "," " "}]]
   ["-h" "--help"]])

(defn usage [summary]
  (->> ["Load, sort and display records."
        ""
        "Usage: recordity [options]"
        ""
        "Options:"
        summary
        ""
        "Example: recordity -f records.txt -d | -f more.txt -d ,"
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

      (first options) ;; TODO
      {:exit-message (format "Options: %s" options) :ok? true}

      :else
      {:exit-message (usage summary)})))

(defn exit-msg [status message]
  (println message)
  (log/log (if (zero? status) :info :error)
           message)
  status)

(defn exit-err [e]
  (let [message (format "Caught %s: %s" (.. e getClass getSimpleName) (.getMessage e))
        status 1]
    (println message)
    (log/error e message)
    status))

(defn main [& args]
  (try
    (let [{:keys [exit-message ok?]} (validate-args args)]
      (exit-msg (if ok? 0 1) exit-message))
    (catch Exception e
      (exit-err e))))

(defn exit [status]
  (System/exit status))

(defn -main [& args]
  (exit (apply main args)))
