(ns cli
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(defn main [& args])

(defn exit [status]
  (System/exit status))

(defn -main [& args]
  (exit (apply main args)))
