;; src/myapp/MyServlet.clj
(ns myapp.MyServlet
  (:use compojure)
  (:gen-class
    :extends javax.servlet.http.HttpServlet))

(defroutes greeter
  (GET "/"
    (html [:h1 "Hello world! What a nice day!"]))
  (GET "/my/"
    (html [:h1 "Hello world! What a nice my day!"]))
)

(defservice greeter)