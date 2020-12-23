(ns restful
  "REST API server"
  (:require
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [compojure.core :refer :all]
   [compojure.route :as route]
   [recordity :as r]
   [ring.adapter.jetty :refer :all]
   [ring.middleware.params :refer (wrap-params)]
   [ring.middleware.session :refer (wrap-session)]
   [ring.middleware.session.memory :refer (memory-store)]))

(def delims {"|" (r/delimiters :pipe)
             "," (r/delimiters :comma)
             " " (r/delimiters :space)})

(def sorts->comparators {"gender" (r/comparators :genderThenLastName)
                         "name" (r/comparators :lastNameDesc)
                         "birthdate" (r/comparators :dob)})

(defn session-records
  "Add the set of records to the `:session` map of the request or response, return the updated
  request or response."
  [request-or-response records]
  (assoc-in request-or-response [:session :records] records))

(defn update-session
  "Middleware for the POST route to update the session records in the response map."
  [handler]
  (fn [{:keys [session] :as request}]
    (let [rcds (session :records #{})
          {:keys [rcd] :as resp} (handler request)]
      (session-records (dissoc resp :rcd) (conj rcds rcd)))))

(def handler
  (wrap-params
   (routes
    (GET "/" [] "Hello Recordity")
    (GET "/records/:sorting" [sorting :as {:keys [session]}]
         (let [rcds (session :records #{})]
           {:status 200
            :headers {"Content-Type" "application/json"}
            :body (json/generate-string (map r/record-str (sort (sorts->comparators sorting) rcds)))}))
    (update-session
     (POST "/records" [record delimiter]
           (let [del (delims delimiter)
                 _ (log/debugf "POSTing, about to parse record %s with delimiter %s" record del)
                 rcd (r/parse-record del r/input-date-format record)]
             {:status 204 :rcd rcd})))
    (route/not-found "Not Found"))))

(defn prod-handler
  "Wraps ring in-memory session management middleware on top of the handler, with session `:records`
  stored as a set in the session map, keyed off of session id (UUID), all inside an `atom`."
  ([]
   (prod-handler (atom {})))
  ([session-map-atom]
   (wrap-session handler {:store (memory-store session-map-atom)
                          :root "/records"
                          :cookie-name "recordity"})))

(defn -main
  [& args]
  (let [port 3000
        msg "Starting Recordity RESTful API jetty server on port"]
    (println msg port)
    (log/info msg port)
    (run-jetty (prod-handler) {:port port})))
