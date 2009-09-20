(enable-!-reader)

;;;; display ;;;;;;;;;;;;;;;;;;;;;;;;;;;

(in-package :triple-store-user)

(defun show (r)
    (print-triples
        (get-triples-list :s r :limit nil) :format :ntriple))
(defun show-in (r)
    (print-triples
        (get-triples-list :o r :limit nil) :format :ntriple))

(defun text (s &optional limit)
    (print-triples (freetext-get-triples s)
    :limit (if (eq limit ()) 10 limit)))

(defun count-triples-per-graph (&key size)
    (let ((h (make-upi-hash-table :size (if (eq () size) 1000 size))))
        (iterate-cursor (tr (get-triples))
            (let ((graph (graph tr)))
                (let ((count (gethash graph h)))
                    (setf (gethash graph h)
                        (if (eq count nil) 1 (+ count 1))))))
        (loop for graph being the hash-key using (hash-value count) of h do
            (format t "~%~70a ~d" graph count))))


;;;; ranking ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defun print-ranking (table)
    (let (seq)
        (maphash
            (lambda (k v)
	        (push (cons k v) seq))
	    table)
        (let ((sorted (sort seq '> :key 'cdr)))
            (dolist (e sorted)
	        (format t "~%~70a ~d" (part->string (car e)) (cdr e))))))

(defun rank (upi-generator &key size)
    ;; 10,000 seems to be a good default size for the hash table.
    ;; It does not cause any appreciable delay.
    (let ((table (make-upi-hash-table :size (if (eq () size) 10000 size))))
        (funcall upi-generator
	    (lambda (upi) (incf (gethash upi table 0))))
	(print-ranking table)))

(use-package :sparql)
(defun rank-from-sparql (query &key size)
    (rank
        (lambda (visit)
	    (dolist
	        (e (run-sparql query :results-format :arrays))
		(funcall visit (elt e 0)))))
	:size size)

(defun rankvector-add-weight (vector key amount)
    (setf (gethash key vector) (+ amount (gethash key vector 0.0))))
(defun rankvector-new-vector (&key size)
    (make-upi-hash-table
        :size (if (eq () size) 10000 size)))
	
	
;;;; ranking applications ;;;;;;;;;;;;;;

(defun rank-type-of-subject-by-property (property)
    (rank
        (lambda (visitor)
            (iterate-cursor (tr (get-triples :p property))
	        (iterate-cursor (tr2 (get-triples :s (subject tr) :p !rdf:type))
	            (funcall visitor (object tr2)))))))
(defun rank-type-of-object-by-property (property)
    (rank
        (lambda (visitor)
            (iterate-cursor (tr (get-triples :p property))
	        (iterate-cursor (tr2 (get-triples :s (object tr) :p !rdf:type))
	            (funcall visitor (object tr2)))))))

;;;; geolocation ;;;;;;;;;;;;;;;;;;;;;;;

(enable-!-reader)
(register-namespace "pos" "http://www.w3.org/2003/01/geo/wgs84_pos#")
(register-namespace "geo" "http://franz.com/ns/allegrograph/3.0/geospatial/fn/")
;;(register-namespace "geo" "http://example.org/geo#")

(defun geo-setup ()
    ;; -> http://franz.com/ns/allegrograph/3.0/geospatial/spherical/miles/-180.0/180.0/-90.0/90.0/5.0
    (defparameter *lat-lon-5*   (register-latitude-striping-in-miles 5.0s0)) ;2
    ;; -> http://franz.com/ns/allegrograph/3.0/geospatial/spherical/miles/-180.0/180.0/-90.0/90.0/100.0
    (defparameter *lat-lon-100* (register-latitude-striping-in-miles 100.0s0)) ;3
    (defparameter *geo-properties*
        '((!pos:long :longitude)
          (!pos:lat :latitude)))
    (add-geospatial-subtype-to-db *lat-lon-5*) ; 4
    (add-geospatial-subtype-to-db *lat-lon-100*)) ; 5

(defun get-latitude (r)
    (let (default)
        (iterate-cursor (tr (get-triples :s r :p !pos:lat))
            (handler-case
	        (return-from get-latitude
	            (+ 0.0 (read-from-string (upi->value (object tr)))))
	        (type-error (e) ())
		(reader-error (e) ())))
    default))
(defun get-longitude (r)
    (let (default)
        (iterate-cursor (tr (get-triples :s r :p !pos:long))
            (handler-case
	        (return-from get-longitude
	            (+ 0.0 (read-from-string (upi->value (object tr)))))
	        (type-error (e) ())
		(reader-error (e) ())))
    default))

;; 
(defun normalize-latitude (lat)
    (if (eq nil lat) nil
        (if (< lat -90) nil
            (if (> lat 90) nil lat))))
	    
;; 
(defun normalize-longitude (lon)
    (if (eq nil lon) nil
        (if (< lon -360) nil
            (if (> lon 360) nil
	        (if (< lon -180) (+ 360 lon)
	            (if (> lon 180) (- 360 lon) lon))))))

;; geo-index a resource with valid pos:long and pos:lat values
(defun index-geopoint (geopoint)
    (let (
        (lat (normalize-latitude (get-latitude geopoint)))
	(lon (normalize-longitude (get-longitude geopoint))))
	(when (and (not (eq nil lat)) (not (eq nil lon)))
	    (with-temp-upi (upi)
	        (add-triple geopoint
	            !geo:isAt5
                    (longitude-latitude->upi *lat-lon-5* lon lat upi)
		    :g !geo:graph)
                (add-triple geopoint
                    !geo:isAt100
                    (longitude-latitude->upi *lat-lon-100* lon lat upi)
		    :g !geo:graph)))))

(defun count-all-geopoints ()
    (defparameter *count* 0)
    (iterate-cursor (tr (get-triples :p !pos:lat))
        (let ((geopoint (subject tr)))
            (let (
	        (lat (normalize-latitude (get-latitude geopoint)))
	        (lon (normalize-longitude (get-longitude geopoint))))
	        (when (and (not (eq nil lat)) (not (eq nil lon)))
	            (setf *count* (+ 1 *count*))))))
    *count*)
	    
(defun index-all-geopoints ()
    (defparameter *geoindexing-in-progress* t)
    ;; Anything with a pos:lat is considered a geopoint
    (iterate-cursor (tr (get-triples :p !pos:lat))
        (index-geopoint (subject tr)))
    (index-new-triples)
    (defparameter *geoindexing-in-progress* nil))
    
(defun delete-all-geo-triples ()
    (delete-triples :g !geo:graph))

;; Note: there are two triples per geopoint
(defun count-geo-triples ()
    (count-cursor (get-triples :g !geo:graph)))


;;;;;;;;;;;;

;(defun starts-with (s prefix)
;    (if (> (length prefix) (length s))
;        nil
;	(string= prefix (subseq s 0 (length prefix)))))
;
;(defun host-part (uri)
;    (if (starts-with uri "http://")
;        (let ((s (subseq s 7)))
;	    
;	nil))


;;;;;;;;;;;




;;;; scp ~/Dropbox/work/internships/Franz/btc/btc.cl josh@rambo:/net/gemini/home/josh

(defun cheating ()
  (let ((ht (make-upi-hash-table :size 10000000))
	(freqht (make-upi-hash-table :size 1000000)))
    (time
     (iterate-cursor (tr (get-triples :p !<http://www.w3.org/2003/01/geo/wgs84_pos#lat>))
       (setf (gethash (subject tr) ht) t)))
    (time
     (iterate-cursor (tr (get-triples :p !rdf:type))
       (when (gethash (subject tr) ht)
	 (incf (gethash (object tr) freqht 0)))))
    (setf *ht* ht *freqht* freqht) ; for later analysis
    (time
     (let ((res nil))
       (maphash
	(lambda (k v)
	  (push (cons k v) res))
	freqht)
       (print (length res))
       (setf res (sort res '> :key 'cdr))
       (dolist (e res)
	 (when (> (cdr e) 50)
	   (format t "~%~70s    ~8d" (part->string (car e)) (cdr e))))))))



(register-namespace "pos" "http://www.w3.org/2003/01/geo/wgs84_pos#")

(defun newestcheat ()
(let ((ht (make-upi-hash-table :size 10000000))
      (freqht (make-upi-hash-table :size 1000000)))
    (iterate-cursor (tr (get-triples :p !pos:lat))
        (setf (gethash (subject tr) ht) t))
    (iterate-cursor (tr (get-triples :p !rdf:type))
        (when (gethash (subject tr) ht)
            (incf (gethash (object tr) freqht 0))))
    (print-ranking freqht)))




(register-namespace "pos" "http://www.w3.org/2003/01/geo/wgs84_pos#")

(defun newcheat ()
    (rank
        (lambda (visit)
            (iterate-cursor (tr (get-triples :p !pos:lat))
                (iterate-cursor (f (get-triples :s (subject tr) :p !rdf:type))
	            (funcall visit (object f)))))))






