(ns dfrss.core
  (:require [net.cgrand.enlive-html :as html]))

(def select html/select)

(def select-fst (comp first select))

(defn fetch-url
  "Retrieves the web page specified by the url and
   makes an html-resource out of it which is used
   by enlive."
  [url] (html/html-resource (java.net.URL. url)))

(defn articles
  "Takes enlive resource and returns a list of whole article elements"
  [resource]
  (select resource [:body :table :tr :td :div.modryram :table :tr :td.z]))

(defn article-name
  [td]
  (->> [:td.z :a.clanek :span.clanadpis]
       (select td)
       first
       :content
       first))

(defn article-url
  [td]
  (->> [:td.z :a.clanek]
       (select td)
       first
       :attrs
       :href))

(defn article-author
  [td]
  (->> [:td.z :a]
       (select td)
       (filter #(->> % :attrs :href (take 6) (= [\m \a \i \l \t \o])))
       first
       :content
       first
       ))

(defn article
  "Takes enlive resource of a single article and returns (name url author)"
   [res]
   [(article-name res) (article-url res) (article-author res)])

(defn foo
  "I don't do a whole lot."
  []
  (->> "http://www.dfens-cz.com/"
       fetch-url
       articles
       (map article)))
