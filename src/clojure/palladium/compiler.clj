(ns palladium.compiler
  (:require [instaparse.core :as insta]))

(def p (insta/parser (clojure.java.io/resource "grammar") :string-ci true))

;;; main functions

(defmulti compile-ast first)

(defmulti typecheck-ast (fn [ast _] (first ast)))

(defn compile-block [ast]
  (clojure.string/join "\n" (map compile-ast ast)))

(defn compile [s]
  (compile-block (p s)))

(defn typecheck [s m]
  (doseq [ast (insta/add-line-and-column-info-to-metadata s (p s))]
    (typecheck-ast ast m)))

;;; helper functions

(defn identifier-type [id]
  (if (= \$ (first (reverse id)))
    :string
    :number))

(defn base-id [id]
  (clojure.string/upper-case
    (if (= :string (identifier-type id))
      (reduce str (butlast id))
      id)))

(defn var-prefix [id]
  (if (= :string (identifier-type id))
    "strings"
    "numbers"))

(defn array-prefix [id]
  (if (= :string (identifier-type id))
    "stringArrays"
    "numberArrays"))

(defn method-prefix [id]
  (if (= :string (identifier-type id))
    "StringMethods"
    "NumberMethods"))

;;; compile-ast multimethod

(defmethod compile-ast :number [ast]
  (str "((double)" (second ast) ")"))

(defmethod compile-ast :string [ast]
  (str "\""
       (clojure.string/replace (second ast)
                               "\"\""
                               "\\\"")
       "\""))

(defmethod compile-ast :true [_]
  "1")

(defmethod compile-ast :false [_]
  "0")

(defmethod compile-ast :not [ast]
  (str "Utils.not(" (compile-ast (second ast)) ")"))

(defmethod compile-ast :call [ast]
  (case (clojure.string/upper-case (second (second ast)))
    "APPENDARRAY" (let [array (clojure.string/upper-case (second (second (nth ast 2))))
                        prefix (array-prefix array)
                        value (compile-ast (nth (nth ast 2) 2))]
                    (str "Utils." prefix ".get(\"" array "\").add(" value ")"))
    "COUNTARRAY" (let [array (clojure.string/upper-case (second (second (nth ast 2))))
                       prefix (array-prefix array)]
                   (str "Utils." prefix ".get(\"" array "\").size()"))
    (let [id (second (second ast))
          prefix (method-prefix id)
          base-id (base-id id)
          args (drop 1 (nth ast 2))
          compiled-args (map compile-ast args)
          args-str (clojure.string/join "," compiled-args)]
      (str prefix "." base-id "(" args-str ")"))))

(defmethod compile-ast :identifier [ast]
  (let [id (second ast)
        base-id (base-id id)
        prefix (var-prefix id)]
    (str "Utils." prefix ".get(\"" base-id "\")")))

(defmethod compile-ast :div [ast]
  (str "("
       (compile-ast (second ast))
       "/"
       (compile-ast (nth ast 2))
       ")"))

(defmethod compile-ast :mult [ast]
  (str "("
       (compile-ast (second ast))
       "*"
       (compile-ast (nth ast 2))
       ")"))

(defmethod compile-ast :add [ast]
  (str "("
       (compile-ast (second ast))
       "+"
       (compile-ast (nth ast 2))
       ")"))

(defmethod compile-ast :sub [ast]
  (str "("
       (compile-ast (second ast))
       "-"
       (compile-ast (nth ast 2))
       ")"))

(defmethod compile-ast :lteq [ast]
  (str "Utils.lteq("
       (compile-ast (second ast))
       ","
       (compile-ast (nth ast 2))
       ")"))

(defmethod compile-ast :gteq [ast]
  (str "Utils.gteq("
       (compile-ast (second ast))
       ","
       (compile-ast (nth ast 2))
       ")"))

(defmethod compile-ast :eq [ast]
  (str "Utils.eq("
       (compile-ast (second ast))
       ","
       (compile-ast (nth ast 2))
       ")"))

(defmethod compile-ast :neq [ast]
  (str "Utils.neq("
       (compile-ast (second ast))
       ","
       (compile-ast (nth ast 2))
       ")"))

(defmethod compile-ast :lt [ast]
  (str "Utils.lt("
       (compile-ast (second ast))
       ","
       (compile-ast (nth ast 2))
       ")"))

(defmethod compile-ast :gt [ast]
  (str "Utils.gt("
       (compile-ast (second ast))
       ","
       (compile-ast (nth ast 2))
       ")"))

(defmethod compile-ast :and [ast]
  (str "Utils.and("
       (compile-ast (second ast))
       ","
       (compile-ast (nth ast 2))
       ")"))

(defmethod compile-ast :or [ast]
  (str "Utils.or("
       (compile-ast (second ast))
       ","
       (compile-ast (nth ast 2))
       ")"))

(defmethod compile-ast :let [ast]
  (if (= 3 (count ast))
    (let [id (second (second ast))
          prefix (var-prefix id)
          base-id (base-id id)]
      (str "Utils." prefix ".put(\"" base-id "\"," (compile-ast (nth ast 2)) ");"))
    (let [id (second (second ast))
          prefix (array-prefix id)
          base-id (base-id id)]
      (str "Utils." prefix ".get(\"" base-id "\").set(" (compile-ast (nth ast 2)) "," (compile-ast (nth ast 3)) ");"))))

(defmethod compile-ast :call-stmt [ast]
  (str (compile-ast (conj (drop 1 ast) :call)) ";"))

(defmethod compile-ast :for [ast]
  (let [from-exp (compile-ast [:let (second ast) (nth ast 2)])
        while-exp (compile-ast [:lteq (second ast) (nth ast 3)])
        next-exp (compile-ast [:let (second ast) [:add (second ast) [:number "1"]]])
        block (compile-block (drop 4 ast))]
    (str from-exp "\nwhile(" while-exp "){\n" block "\n" next-exp "\n}")))

(defmethod compile-ast :if [ast]
  (let [first-line (str "if(" (compile-ast (second ast))  "!=0){\n")
        then (if (= :then (first (nth ast 2)))
               (compile-block (drop 1 (nth ast 2)))
               (compile-ast (nth ast 2)))
        else (if (= 4 (count ast))
               (compile-block (drop 1 (nth ast 3))))]
    (str first-line then "\n}else{\n" else "\n}\n")))

(defmethod compile-ast :return [ast]
  (let [exp (if (= 2 (count ast))
              (compile-ast (second ast)))]
    (str "return " exp ";")))

(defmethod compile-ast :dim [ast]
  (let [id (second (second ast))
        array (array-prefix id)
        constructor (if (= (identifier-type id) :string)
                      "String"
                      "Double")
        base-id (base-id id)]
    (str array ".put(\"" base-id "\",new ArrayList<" constructor ">(" (compile-ast (nth ast 2)) "));")))

(defmethod compile-ast :array_get [ast]
  (let [id (second (second ast))
        base-id (base-id id)
        array (array-prefix id)]
    (str array ".get(\"" base-id "\").get(" (compile-ast (nth ast 2)) ")")))


;;; typecheck-ast multimethod

(defn line-info [ast]
  (let [meta (meta ast)
        l (:instaparse.gll/start-line meta)
        sc (:instaparse.gll/start-column meta)
        ec (:instaparse.gll/end-column meta)]
    (str "Line " l ", columns " sc "-" ec)))

(defn assert-type [type1 type2 ast]
  (if (not= type1 type2)
    (throw (AssertionError. (str "Typecheck exception! Expected " type2 " but got " type1 "\n" (line-info ast))))
    type2))

(defmethod typecheck-ast :number [_ _]
  :number)

(defmethod typecheck-ast :string [_ _]
  :string)

(defmethod typecheck-ast :true [_ _]
  :number)

(defmethod typecheck-ast :false [_ _]
  :number)

(defmethod typecheck-ast :identifier [ast _]
  (identifier-type (second ast)))

(defmethod typecheck-ast :not [ast m]
  (assert-type (typecheck-ast (second ast) m) :number ast))

(defmethod typecheck-ast :call [ast m]
  (let [fn-name (clojure.string/upper-case (second (second ast)))]
    (if (m fn-name)
      (let [n-args (if (= 2 (count ast))
                     0
                     (count (drop 1 (nth ast 2))))
            n-args-in-map (- (count (m fn-name)) 1)]
        (if (= n-args n-args-in-map)
          (if (= n-args 0)
            ((m fn-name) 0)
            (let [args (drop 1 (nth ast 2))
                  arg-types (drop 1 (m fn-name))]
              (dotimes [i n-args]
                (assert-type (typecheck-ast (nth args i) m) (nth arg-types i) ast))
              ((m fn-name) 0)))
          (throw (AssertionError. (str "Wrong number of arguments to method " fn-name "\n" (line-info ast))))))
      (throw (AssertionError. (str "Method doesn't exist: " fn-name "\n" (line-info (second ast))))))))

(defn typecheck-binary-op [ast m]
  (assert-type (typecheck-ast (second ast) m) :number ast)
  (assert-type (typecheck-ast (nth ast 2) m) :number ast))

(defmethod typecheck-ast :div [ast m]
  (typecheck-binary-op ast m))

(defmethod typecheck-ast :mult [ast m]
  (typecheck-binary-op ast m))

(defmethod typecheck-ast :sub [ast m]
  (typecheck-binary-op ast m))

(defmethod typecheck-ast :add [ast m]
  (let [type1 (typecheck-ast (second ast) m)
        type2 (typecheck-ast (nth ast 2) m)]
    (if (not= type1 type2)
      (throw (AssertionError. (str "Typecheck exception! Addition or concatenation?\n" (line-info ast))))
      type1)))

(defmethod typecheck-ast :lteq [ast m]
  (typecheck-binary-op ast m))

(defmethod typecheck-ast :gteq [ast m]
  (typecheck-binary-op ast m))

(defmethod typecheck-ast :lt [ast m]
  (typecheck-binary-op ast m))

(defmethod typecheck-ast :gt [ast m]
  (typecheck-binary-op ast m))

(defmethod typecheck-ast :eq [ast m]
  (let [type1 (typecheck-ast (second ast) m)
        type2 (typecheck-ast (nth ast 2) m)]
    (if (not= type1 type2)
      (throw (AssertionError. (str "Typecheck exception! Comparison between different types\n" (line-info ast))))
      type1)))

(defmethod typecheck-ast :neq [ast m]
  (let [type1 (typecheck-ast (second ast) m)
        type2 (typecheck-ast (nth ast 2) m)]
    (if (not= type1 type2)
      (throw (AssertionError. (str "Typecheck exception! Comparison between different types\n" (line-info ast))))
      type1)))

(defmethod typecheck-ast :and [ast m]
  (typecheck-binary-op ast m))

(defmethod typecheck-ast :or [ast m]
  (typecheck-binary-op ast m))

(defmethod typecheck-ast :let [ast m]
  (if (= 3 (count ast))
    (assert-type (typecheck-ast (nth ast 2) m) (typecheck-ast (second ast) m) ast)
    (do
      (assert-type (typecheck-ast (nth ast 2) m) :number ast)
      (assert-type (typecheck-ast (nth ast 3) m) (typecheck-ast (second ast) m) ast)))
  nil)

(defmethod typecheck-ast :for [ast m]
  (assert-type (typecheck-ast (second ast) m) :number (second ast))
  (assert-type (typecheck-ast (nth ast 2) m) :number (nth ast 2))
  (assert-type (typecheck-ast (nth ast 3) m) :number (nth ast 3))
  (doseq [stmt (drop 4 ast)]
    (typecheck-ast stmt m)))

(defmethod typecheck-ast :if [ast m]
  (assert-type (typecheck-ast (second ast) m) :number (second ast))
  (if (= :then (first (nth ast 2)))
    (doseq [stmt (drop 1 (nth ast 2))]
      (typecheck-ast stmt m))
    (typecheck-ast (nth ast 2) m))
  (if (= 4 (count ast))
    (doseq [stmt (drop 1 (nth ast 3))]
      (typecheck-ast stmt m))))

(defmethod typecheck-ast :return [ast m]
  (if (= 2 (count ast))
    (assert-type (typecheck-ast (second ast) m) (:this m) (second ast))
    (assert-type :void (:this m) ast)))

(defmethod typecheck-ast :dim [ast m]
  (assert-type (typecheck-ast (nth ast 2) m) :number (nth ast 2))
  nil)

(defmethod typecheck-ast :array_get [ast m]
  (assert-type (typecheck-ast (nth ast 2) m) :number (nth ast 2))
  (identifier-type (second (second ast))))

(defmethod typecheck-ast :call-stmt [ast m]
  (typecheck-ast (conj (drop 1 ast) :call) m)
  nil)
