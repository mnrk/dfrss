(ns dfrss.core
  (:use ring.adapter.jetty)
  (:require [net.cgrand.enlive-html :as html])
  (:require [clj-rss.core :as rss]))

(def select html/select)

(def select-fst (comp first select))

(defn fetch-url
  "Retrieves the web page specified by the url and
   encoding and makes an html-resource out of it which
   is used by enlive."
  [encoding url]
  (-> url
      java.net.URL.
      .getContent (java.io.InputStreamReader. encoding)
      html/html-resource))


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

(defn feed
  [articles]
  (->> articles
       (map (fn [[name url auth]] {:title name :link url :description auth}))
       (apply rss/channel-xml)))

(defn content
  "I don't do a whole lot."
  []
  (->> "http://www.dfens-cz.com/"
       (fetch-url "Cp1250")
       articles
       (map article)
       feed))

(defn handler
  [req]
  {:status  200
   :headers {"Content-Type" "text/html;charset=UTF-8"}
   :body    (content)})

(defn boot []
  (run-jetty #'handler {:port 8080}))