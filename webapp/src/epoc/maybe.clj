(ns maybe
  (:use clojure.contrib.monads))

;; My Maybe monad

(+ 3 3)

(defmonad identity-m
   "Monad describing plain computations. This monad does in fact nothing
    at all. It is useful for testing, for combination with monad
    transformers, and for code that is parameterized with a monad."
  [m-result identity
   m-bind   (fn m-result-id [mv f]
	      (f mv))
  ])


; Maybe monad
(defmonad maybe-m
   "Monad describing computations with possible failures. Failure is
    represented by nil, any other value is considered valid. As soon as
    a step returns nil, the whole computation will yield nil as well."
   [m-zero   nil
    m-result (fn m-result-maybe [v] v)
    m-bind   (fn m-bind-maybe [mv f]
               (if (nil? mv) nil (f mv)))
    m-plus   (fn m-plus-maybe [& mvs]
	       (first (drop-while nil? mvs)))
    ])


; Maybe monad
(defmonad mymaybe-m
   "Monad describing computations with possible failures. Failure is
    represented by nil, any other value is considered valid. As soon as
    a step returns nil, the whole computation will yield nil as well."
   [m-zero   nil
    m-result identity
    m-bind   (fn m-bind-maybe [mv f]
               (if (nil? mv) nil (f mv)))
    ])

; First non-null monad
(defmonad first-non-null-m
   [m-zero   nil
    m-result identity
    m-bind   (fn m-bind-maybe [mv f]
               (if (not (nil? mv)) mv (f mv)))
    ])

(domonad first-non-null-m
 [
  file (let [f (java.io.File. "/home/malcolm/foo")] (if (.exists f) f nil))
  file2 (let [f (java.io.File. "/home/malcolm/bar")] (if (.exists f) f nil))
  file3 (let [f (java.io.File. "/home/malcolm/zip")] (if (.exists f) f nil))
  ]
 (throw (Exception. "No file"))
 )

;; result Monadic value

