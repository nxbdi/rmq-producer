(ns rmq-producer.main
  (:use [clojure.tools.cli :only [cli]])
  (:require [rmq-producer.core :as core])
  (:gen-class))

(defn parse-args [argv]
  (cli argv ["-h" "--host" "RMQ host"]
       ["-P" "--port" "RMQ port" :parse-fn #(Integer. %) :default 5672]
       ["-s" "--source-queue" "The name of a source queue to use, instead of stdin." :default nil]
       ["-x" "--exchange" "RMQ exchange"]
       ["-v" "--vhost" "RMQ vhost" :default "/"]
       ["-u" "--username" "RMQ username" :default "guest"]
       ["-p" "--password" "RMQ password" :default "guest"]
       ["-m" "--message-template" "Message template" :default nil]
       ["-t" "--content-type" "Message content type" :default "application/json"]
       ["-e" "--content-encoding" "Message encoding" :default "utf8"]
       ["-k" "--routing-key" "Message routing key" :default ""]
       ["-f" "--field-separator" "If using stdin, separator for message parts." 
        :parse-fn #(re-pattern %) :default nil]
       ["-v" "--verbose" "Enable verbose messages." :default false :flag true]
       ["-x" "--exit-on-empty" "Exit when the source queue is empty" :default false :flag true]
       ))

(defn read-and-publish [publish-message field-separator]
  (let [input (read-line)]
    (when (not (nil? input))
      (->
       (if field-separator 
         (clojure.string/split input field-separator)
         [input])
       (publish-message))
      (recur publish-message field-separator))))

(defn consume-and-publish [publish-message consume-message exit-on-empty]
  (let [input (consume-message)]
    (if (not (nil? input))
      (do
        (publish-message [input])
        (recur publish-message consume-message exit-on-empty))
      (when-not exit-on-empty
        (recur publish-message consume-message exit-on-empty)))))

(defn -main [& argv]
  (let [parsed-args (parse-args argv)
        settings (first parsed-args)
        field-separator (:field-separator settings)
        verbose (:verbose settings)
        exit-on-empty (:exit-on-empty settings)
        log (if verbose println (fn [&args] 0))]

    (if (= 0 (count argv))
      (do
        (println (last parsed-args))
        (System/exit 0)))

    (if (nil? (:source-queue settings))
      (do
        (let [[publish-message close-connection] (core/mk-rmq-publish-and-close-fns settings log)]
          (log "consuming from stdin ...")
          (read-and-publish publish-message field-separator)
          (close-connection)))
      (let [[publish-message close-publisher-connection] (core/mk-rmq-publish-and-close-fns settings log)
            [consume-message close-consumer-connection] (core/mk-rmq-consume-and-close-fns settings log)]
        (log "consuming from a source queue ...")
        (consume-and-publish publish-message consume-message exit-on-empty)
        (close-publisher-connection)
        (close-consumer-connection)))))
