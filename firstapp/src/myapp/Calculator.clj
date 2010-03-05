(ns myapp.Calculator
  (:use compojure)
  (:gen-class
    :extends javax.servlet.http.HttpServlet))

(defn html-doc
  [title & body]
  (html
    (doctype :html4)
    [:html
      [:head
        [:title title]]
      [:body
       [:div
	[:h2
	 ;; Pass a map as the first argument to be set as attributes of the element
	 [:a {:href "/calc/"} "Home"]]]
        body]]))


(def sum-form
  (html-doc "Sum"
    (form-to [:post "/calc/"]
      (text-field {:size 3} :x)
      "+"
      (text-field {:size 3} :y)
      (submit-button "="))))

(defn result
  [x y]
  (let [x (Integer/parseInt x)
        y (Integer/parseInt y)]
    (html-doc "Result"
      x " + " y " = " (+ x y))))

(defroutes webservice
  (GET "/calc/"
    sum-form)
  (POST "/calc/"
    (result (params :x) (params :y))))

(defservice webservice)