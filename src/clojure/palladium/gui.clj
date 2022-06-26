(ns palladium.gui
  (:import [javax.swing JFrame JTextArea]))

(def frame (JFrame. "PalladiumCreator"))

(def text-area (JTextArea.))

(defn config-text-area []
  (let [font (.getFont text-area)
        new-font (.deriveFont font (float 16.0))]
    (doto text-area
      (.setFont new-font)
      (.setColumns 80)
      (.setRows 60))))

(defn show-frame []
  (doto frame
    (.add text-area)
    (.pack)
    (.setVisible true)))
