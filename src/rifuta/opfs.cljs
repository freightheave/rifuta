(ns rifuta.opfs)

(defn write
  "Writes content-str to OPFS in a file named file-name,
  ovewriting the file if it exists.
 ASYNC, returns a promise, that resolves to <undefined>."
  [file-name, content-str]
  (-> (js/navigator.storage.getDirectory)
      (.then #(.getFileHandle % file-name (clj->js {:create true})))
      (.then #(.createWritable %))
      (.then (fn [stream]
               (.then (.write stream content-str)
                      (fn [_]
                        (.close stream)))))
      (.catch (fn [error] (js/console.log "failed writing:" error)))))

(defn read
  "ASYNC, Returns a promise that resolves to the contents of filename in OPFS as string.
  If file-name doesn't exist, the promise returned is rejected with 'DOMException: Entry not found'."
  [file-name]
  (-> (js/navigator.storage.getDirectory)
      (.then #(.getFileHandle % file-name))
      (.then #(.getFile %))
      (.then #(.text %))))
