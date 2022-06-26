(ns palladium.core
  (:gen-class)
  (:import (palladium GUI)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (GUI.))

(def methods-map
  {"PRINT" [:void :string]
   "STR$"  [:string :number]})

(defn build [code]
  (try
    (palladium.compiler/typecheck code methods-map)
    (with-open [f (clojure.java.io/writer "javasrc/Main.java")]
      (binding [*out* f]
        (print "class Main {\npublic static void main(String[] args) {\nUtils.init();\n")
        (print (palladium.compiler/compile code))
        (print "\n}\n}")))))

;; TODO - invoke build script
