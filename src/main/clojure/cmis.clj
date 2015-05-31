(ns cmis
  (:import [java.util Date]
           [java.text SimpleDateFormat]
           [org.apache.chemistry.opencmis.client.api CmisObject]
           [java.util GregorianCalendar]
           [org.apache.chemistry.opencmis.client.runtime SessionFactoryImpl]
           [org.apache.chemistry.opencmis.commons SessionParameter]
           [java.util HashMap]))

(println "preparing functions")

(def sessionfactory (SessionFactoryImpl/newInstance))

(defn create-session [repo user password]
  (def session (.createSession sessionfactory (HashMap. {SessionParameter/BINDING_TYPE "browser"
                                                         SessionParameter/BROWSER_URL "http://localhost:8080/browser"
                                                         SessionParameter/USER user
                                                         SessionParameter/PASSWORD password
                                                         SessionParameter/AUTH_HTTP_BASIC "true"
                                                         SessionParameter/AUTH_SOAP_USERNAMETOKEN "true"
                                                         SessionParameter/COOKIES "true"
                                                         SessionParameter/COMPRESSION "true"
                                                         SessionParameter/REPOSITORY_ID repo}))))
;(def calendar-format "MMM dd,yyyy HH:mm")
;;; fn to change format at runtime
;(defn set-calendar-format [format]
;  (def calendar-format format))
;
;(defn format-date [calendar]
;  (.format (SimpleDateFormat. calendar-format) (.getTime calendar)))


;; get object by id
(defn id [id] (.getObject session id))
;; get object by path
(defn path [path] (.getObjectByPath session path))

;;print one property
(defn print-prop [prop]
  (printf "%-40s %s\n" (.getId prop) (vec (.getValues prop))))

;; print all properties of one object
(defn props [object & args]
  (let [obj (if (instance? CmisObject object) object (id object))]
    (let [props (.getProperties obj)]
      (if (nil? args)
        (doseq [p props] (print-prop p))
        (doseq [p args] (print-prop (.getProperty obj p)))))))


;; print the children
(defn ls [d]
  (let [dir (if (instance? CmisObject d) d (id d))]
    (println "Objects in " (.getName dir) (.getId dir) ": ")
    (println (apply str (repeat 80 "-")))
    (doseq [f (.getChildren dir)]
      (printf "%-30.30s %-40s %s\n" (.getName f) (.getId f) (.getBaseTypeId f)))))

;; get all children
(defn children [d]
  (let [dir (if (instance? CmisObject d) d (id d))]
    (seq (.getChildren dir))))


(defn print-changes
  ([] (print-changes nil nil))
  ([token] (print-changes token nil))
  ([token maxitems]
    (let [changes (.getChangeEvents (.getContentChanges session token true (if (nil? maxitems) 100 maxitems)))]
      (printf "%-40s %-40s %-10s  %-20.20s %s\n" "Token" "ObjectId" "Type" "Name" "Time")
      (println (apply str (repeat 140 "-")))
      (doseq [c changes] (printf "%-40s %-40s %-10s  %-20.20s %s\n"
                           (.. c getProperties (get "cmis:changeToken"))
                           (.getObjectId c)
                           (.getChangeType c)
                           (.. c getProperties (get "cmis:name"))
                           (.getTime (.getChangeTime c))))
      (println (.size changes) " changes ")
      )))


;;(def root (.getRootFolder session))

