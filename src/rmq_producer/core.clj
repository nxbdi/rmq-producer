(ns rmq-producer.core
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.exchange  :as le]
            [langohr.queue     :as lq]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb])
  (:gen-class))

(defn apply-message-template [message-template data]
  (apply (partial clojure.core/format message-template) data))

(defn mk-rmq-consume-and-close-fns [settings log]
  (let [{:keys [host port username password source-queue vhost message-template content-type content-encoding routing-key]} settings
        conn  (rmq/connect {:host host :port port :username username :password password :vhost vhost})
        ch    (lch/open conn)]
    [(fn []
      (let [[metadata payload] (lb/get ch source-queue)]
        (if (not (nil? payload)) 
          (do
            (log (str "reading message:" (String. payload)))
            (String. payload))
          nil)))
     (fn [] 
       (rmq/close ch)
       (rmq/close conn))]))

(defn mk-rmq-publish-and-close-fns [settings log]
  (let [{:keys [host port username password exchange vhost message-template content-type content-encoding routing-key]} settings
        conn  (rmq/connect {:host host :port port :username username :password password :vhost vhost})
        ch    (lch/open conn)]
    [(fn [data]
       (let [message (if (not (nil? message-template)) (apply-message-template message-template data) (first data))]
         (do 
           (log (str "publishing message: " message))
           (lb/publish ch exchange routing-key message :content-type content-type :content-encoding content-encoding))))
     (fn []
       (rmq/close ch)
       (rmq/close conn))]))
