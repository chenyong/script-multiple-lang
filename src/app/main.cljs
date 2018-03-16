
(ns app.main (:require ["fs" :as fs] [clojure.string :as str]))

(defn add-locale-map [acc locale-map mark]
  (if (empty? locale-map)
    acc
    (let [[k v] (first locale-map)
          next-acc (if (and (contains? acc k) (not= (get acc k) v))
                     (do
                      (println)
                      (println "duplicated:" (pr-str k))
                      (println "changing:" (get acc k) "---->" v "....in" mark)
                      (assoc acc (str k "__" mark) v))
                     (assoc acc k v))]
      (recur next-acc (rest locale-map) mark))))

(defonce config-data (fs/readFileSync "data/config.text" "utf8"))

(defonce fi-data (fs/readFileSync "data/fi.text" "utf8"))

(defn load-to-map [content]
  (let [lines (str/split-lines content)]
    (->> lines
         (map
          (fn [line]
            (let [[k v] (str/split line (re-pattern "\\:\\s+"))]
              [k (subs v 1 (- (count v) 2))])))
         (into {}))))

(defonce shared-data (fs/readFileSync "data/shared.text" "utf8"))

(defn main! []
  (let [shared-map (load-to-map shared-data)
        fi-map (load-to-map fi-data)
        config-map (load-to-map config-data)
        all-in-one-map (-> {}
                           (add-locale-map shared-map "shared")
                           (add-locale-map fi-map "FI")
                           (add-locale-map config-map "config"))]
    (println
     (str/join
      "\n"
      (->> all-in-one-map
           (sort-by first)
           (map
            (fn [[k v]]
              (str k ": \"" (str/replace v "\"" "\\\"") "\",")
              (str k ": string;"))))))))

(defn reload! [] (main!))
