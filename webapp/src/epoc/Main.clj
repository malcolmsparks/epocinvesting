(ns epoc.Main
  (:use compojure)
  (:gen-class
   :extends javax.servlet.http.HttpServlet))

(defn get-data-as-reader []
  (java.io.BufferedReader. (java.io.FileReader. (java.io.File. "data/20091129.csv"))))

(def factors [:value :momentum :revisions :risk :quality :growth :price])

(def csv-columns
     (apply vector (concat [:name :ticker :reserved] factors [:sector])))

(defn parse-line [line] (zipmap csv-columns (.split line ",")))

;; This is used to take a random amount of data.
(defn some-of [] (int (+ 10 (rand 49))))

(defn data []
  (map parse-line (drop 3 (line-seq (get-data-as-reader)))))

;; Compare order of 2 strings (ascending)
(defn s< [l r]
  (.compare (java.text.Collator/getInstance) l r))

;; Compare order of 2 strings (descending)
(defn s> [l r]
  (.compare (java.text.Collator/getInstance) r l))

(defn make-table-row [field r]
  [:tr
   [:td (:name r)]
   [:td (:ticker r)]
   [:td (field r)]
   ]
  )

(defn titlecase [keyword]
  (reduce str
   (java.lang.Character/toTitleCase (char (first (.getName keyword))))
   (rest (.getName keyword))))

(defn make-table [data last]
  [:table
   [:tr
    [:th "Name"]
    [:th "Ticker"]
    [:th (titlecase last)]
    ]
   (map (partial make-table-row last) data)
   ]
)

;; Useful for the copyright year.
(defn get-year []
  (.get (java.util.Calendar/getInstance) java.util.Calendar/YEAR))

(defn footer []
  [:div#footer
   [:hr] [:p (format "Copyright &copy; EPOC Investing, %d" (get-year))]]
)

(defn template [title & body]
  (html
   [:html
    [:head
     [:title title]
     [:link {:rel "stylesheet" :href "style.css" :type "text/css"}]
     ]
    [:body [:h1#title "ACME Investing"] [:h2 title] body (footer)]]))

(defn menu []
  [:p "Best by... " (interpose " | " (map (fn [n] [:a {:href (str "top-" (.getName n) ".html")} (titlecase n)]) factors))])

(defn index [request]
  (let [data (data)]
    {:status  200
     :headers {}
     :body
     (template "Welcome to EPOC Investing."
       [:p [:a {:href "guide.html"} "Behind our investment recommendations."]]
       (menu)
       [:ul
        [:li [:a {:href "feed.xml"} "XML Feed!"]]
        [:li [:a {:href "charts.html"} "Charting!"]]
        ]
       [:p (format "There are %d companies in this table." (count data))]
       (make-table (sort-by :name s< data) :sector)
       )}))

(defn guide [request]
  {:status  200
   :headers {}
   :body
   (template "Guide"
     [:p "This is our investment guide. We use a strategy for coming up with equity recommendations we call the 'style timer'."]
     [:a {:href "index.html"} "Home"])})

;; Redirect to an index page.
(defn welcome [request]
  {:status 302 :headers { "Location" "index.html" }})

(defn css [request]
  {:status 200 :headers {}
   :body "
body {
  font-family: Liberation Sans, Georgia;
  font-size: 1.4em;
  margin-left: 20px;
}
h1#title {
  font-family: Georgia;
  border: 2px dotted blue;
  padding: 2pt;
  margin-left: 0em;
}
table {
  border: 1px solid black;
  font-size: 1.0em;
}
th {
  padding: 2pt;
  text-align: left;
}
td {
  border-top: 1px solid black;
  padding: 2pt;
}
div#footer {
  margin-top: 30pt;
  font-size: 60%;
}
"})

(defn top [request]
  {:status 200
   :headers {}
   :body
   (let [by (-> request :route-params :factor keyword)]
     (template (str "Best by " (name by))
       [:p [:a {:href "index.html"} "Home"]]
       (menu)
       (make-table (take 10 (sort-by by s> (data))) by)
))})

(defroutes webservice
  (GET "/" welcome)
  (GET "/favicon.ico" {:status 404, :headers {}})
  (GET "/index.html" index)
  (GET "/style.css" css)
  (GET "/guide.html" guide)
  (GET "/top-:factor.html" top)
)

(defn with-header [handler header value]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers header] value))))

(decorate index
          (with-header "X-Lang" "Clojure")
          (with-header "X-Framework" "Compojure"))

(decorate css
          (with-header "Content-Type" "text/css"))

(defservice webservice)

;;(run-server {:port 9}
;;            "/*" (servlet webservice))
