(ns restful-test
  (:require
   [cheshire.core :as json]
   [clojure.test :refer :all]
   [recordity :as r]
   [restful :refer :all]
   [java-time :as jt]
   [ring.mock.request :as mock]))

(def test-records #{(r/record "Kelliot" "Kris" "f" "red" (jt/local-date 1961 2 13))
                    (r/record "Jabar" "Aaron" "m" "blue" (jt/local-date 1955 8 12))
                    (r/record "Smith" "John" "m" "green" (jt/local-date 1953 1 2))})

(deftest test-post-single-record
  (let [expected (conj test-records (r/record "Barkley" "Farnsworth" "m" "brownish"
                                              (jt/local-date 1962 11 22)))]
    (letfn [(post-rcd [r d]
              (select-keys
               (handler (-> (mock/request :post "/records")
                            (mock/body {:record r :delimiter d})
                            (session-records test-records)))
               [:status :session]))]
      (are [r d] (= {:status 204 :session {:records expected}} (post-rcd r d))
        "Barkley,Farnsworth,m,brownish,1962/11/22" ","
        "Barkley|Farnsworth|m|brownish|1962/11/22" "|"
        "Barkley Farnsworth m brownish 1962/11/22" " "))))

(deftest test-get-sorted
  ;; response body should be stringified json
  (testing "sorted by gender"
    (let [expected (json/generate-string ["Kelliot Kris f red 2/13/1961"
                                          "Jabar Aaron m blue 8/12/1955"
                                          "Smith John m green 1/2/1953"])
          {:keys [status body]} (handler (-> (mock/request :get "/records/gender")
                                             (session-records test-records)))]
      (is (= 200 status))
      (is (= expected body))))
  (testing "sorted by birthdate"
    (let [expected (json/generate-string ["Smith John m green 1/2/1953"
                                          "Jabar Aaron m blue 8/12/1955"
                                          "Kelliot Kris f red 2/13/1961"])
          {:keys [status body]} (handler (-> (mock/request :get "/records/birthdate")
                                             (session-records test-records)))]
      (is (= 200 status))
      (is (= expected body))))
  (testing "sorted by name"
    (let [expected (json/generate-string [
                                          "Smith John m green 1/2/1953"
                                          "Kelliot Kris f red 2/13/1961"
                                          "Jabar Aaron m blue 8/12/1955"
                                          ])
          {:keys [status body]} (handler (-> (mock/request :get "/records/name")
                                             (session-records test-records)))]
      (is (= 200 status))
      (is (= expected body)))))

(comment

  ;; jetty server with test records
  (require '(cli))
  (require '(ring.adapter (jetty)))
  (def session-uuid (java.util.UUID/randomUUID))
  (def session (atom {session-uuid {:records (set (take 5 (cli/records [["pipe-delimited.txt" "|"]] "G")))}}))
  (def server (atom nil))
  (future (reset! server (ring.adapter.jetty/run-jetty (prod-handler session) {:port 3000})))
  (.stop @server)


  ;; example body-string:
  ;;"record=Barkley%2CFarnsworth%2Cm%2Cbrownish%2C1962%2F11%2F22&delimiter=%2C"
  (require '(ring.util (request)))
  (-> (mock/request :post "/records")
      (mock/body {:record "Barkley,Farnsworth,m,brownish,1962/11/22"
                  :delimiter ","})
      (ring.util.request/body-string))

  )
