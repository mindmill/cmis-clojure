(ns cmisbindings
  (:import [org.apache.chemistry.opencmis.commons.spi CmisBinding Holder]
           [org.apache.chemistry.opencmis.client.bindings CmisBindingFactory]
           [java.util Date]
           [CmisBindingnding]
           [java.text SimpleDateFormat]
           [org.apache.chemistry.opencmis.commons SessionParameter]
           [java.util HashMap]
           [org.apache.chemistry.opencmis.commons.enums IncludeRelationships]
           [org.apache.chemistry.opencmis.commons.data ObjectData]
           [java.math BigInteger]))

(println "preparing functions")

(def bindingsfactory (CmisBindingFactory/newInstance))

(defn create-binding [repo user password]
  (def cmisbinding (.createCmisAtomPubBinding bindingsfactory (HashMap. {
                                                                          SessionParameter/ATOMPUB_URL "http://localhost:8080/atom11"
                                                                          SessionParameter/USER user
                                                                          SessionParameter/PASSWORD password
                                                                          SessionParameter/REPOSITORY_ID repo})))
  (def current-repo repo))




;; get object by id
(defn id [id]
  (.getObject (.getObjectService cmisbinding) current-repo id "*" true IncludeRelationships/BOTH "cmis:none" true true nil))
;; get object by path
(defn path [path]
  (.getObjectByPath (.getObjectService cmisbinding) current-repo path "*" true IncludeRelationships/BOTH "cmis:none" true true nil))

;;print one property
(defn print-prop [prop]
  (printf "%-40s %s\n" (.getId prop) (vec (.getValues prop))))

;; print all properties of one object
(defn props [object & args]
  (let [obj (if (instance? ObjectData object) object (id object))]
    (let [props (.getProperties (.getProperties obj))]
      (if (nil? args)
        (doseq [p props] (print-prop (.getValue p)))
        (doseq [p args] (print-prop (.get (.getProperties (.getProperties obj)) p)))))))

;; get all children
(defn children [d]
  (let [children (.getChildren (.getNavigationService cmisbinding) current-repo d "*" nil true IncludeRelationships/BOTH "cmis:none" true nil nil nil)]
    (map #(.getObject %1) (.getObjects children))))


;; print the children
(defn ls [d]
  (let [children (children d)
        dir (id d)]
    (println "Objects in " (.getId dir) ": ")
    (println (apply str (repeat 80 "-")))
    (doseq [f children]
      (printf "%-30.30s %-40s %s\n" (.getFirstValue (get (.getProperties (.getProperties f)) "cmis:name")) (.getId f) (.getBaseTypeId f)))
    (println (.size children) " objects")))




(defn print-changes
  ([] (print-changes nil nil))
  ([token] (print-changes token nil))
  ([token maxitems]
    (let [changes (.getObjects (.getContentChanges (.getDiscoveryService cmisbinding) current-repo
                                 (if (nil? token) (Holder.) (Holder. token)) true "*" true false
                                 (if (nil? maxitems) (BigInteger. "100") (BigInteger. (str maxitems))) nil))]
      (printf "%-40s %-40s %-10s  %-20.20s %s\n" "Token" "ObjectId" "Type" "Name" "Time")
      (println (apply str (repeat 140 "-")))
      (doseq [c changes] (let [event (.getChangeEventInfo c)
                               props (.getProperties c)]

                           (printf "%-40s %-40s %-10s  %-20.20s %s\n"
                             (.. props getProperties (get "cmis:changeToken") getFirstValue)
                             (.. props getProperties (get "cmis:objectId") getFirstValue)
                             (.getChangeType event)
                             (.. props getProperties (get "cmis:name") getFirstValue)
                             (.getTime (.getChangeTime event)))))
      (println (.size changes) " changes ")
      )))



;;(create-binding "reponame" "username" "password" )
;;(ls "root")
;;(print changes)
;;;(print-children "root")
