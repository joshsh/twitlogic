(require :agraph)
(in-package :triple-store-user)
(enable-!-reader)
(register-namespace "tl" "http://fortytwo.net/2009/10/twitlogic#")


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; weighted vectors
;;
;; Notes:
;; * eq is used for equality
;; * weights are expected to be positive numbers (never zero and never negative)

(defun create-weighted-vector (&key size)
;;    (make-hash-table
    (make-upi-hash-table
        :size (if (eq () size) 1000 size)))

(defun weighted-vector-magnitude (vector)
    (let ((mag 0.0))
        (loop for v being the hash-value of vector do
            (setq mag (+ mag v)))
        mag))

(defun normalize-weighted-vector (vector)
    (let ((mag (weighted-vector-magnitude vector)))
        (loop for k being the hash-key using (hash-value v) of vector do
            (setf (gethash k vector) (/ (gethash k vector) mag)))))

(defun get-weight (weighted-vector node)
    (gethash node weighted-vector))

;; Note: weight must be a positive number
(defun set-weight (weighted-vector node weight)
    (setf (gethash node weighted-vector) weight))

(defun increment-weight (weighted-vector node weight)
    (setf (gethash node weighted-vector)
        (+ weight (gethash node weighted-vector 0.0))))

;; Note: sorts the key/value list in order of descending value
(defun show-weighted-vector (vector)
    (let ((unsorted nil))
        (loop for k being the hash-key using (hash-value v) of vector do
            (push (list k v) unsorted))
	(loop for pair in
	    (sort unsorted (lambda (a b) (> (first (rest a)) (first (rest b)))))
        do
            (format t "~a: ~a~%" (first pair) (first (rest pair))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; spreading activation

(defun spread (start-vector step-callback maxdepth steps)
    (let ((cur nil) (result (create-weighted-vector)))
        (let ((report-step-callback
            (lambda (node weight)
                (setq cur node)
                (increment-weight result node weight))))
            (loop for i from 0 to (- steps 1) do
	        ;; occasionally reset to a random starting point
		;;(print (eq 0 (mod i maxdepth)))
                (if (eq 0 (mod i maxdepth))
	            (setq cur (choose-random-vector-element-by-weight start-vector)))
            (funcall step-callback cur report-step-callback)))
	(show-weighted-vector result)))
	

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; spreading activation in Association networks

;; TODO: use the optional :graphs parameter
(defun visit-associated-objects (node visitor &key graphs)
    (iterate-cursor (t1 (get-triples :o node :p !tl:subject))
        (let ((assoc (subject t1)))
	    ;; Note: no need to specify the named graph once we have the
	    ;; specific Association
	    ;; TODO: there should never be more than one weight, but iterating
	    ;; over the cursor seems like overkill anyway.
	    (iterate-cursor (t2 (get-triples :s assoc :p !tl:weight))
	        (let ((weight (read-from-string (upi->value (object t2)))))
		    (iterate-cursor (t3 (get-triples :s assoc :p !tl:object))
		        (funcall visitor (object t3) weight)))))))

;; Note: we assume at most one Association per unique object
;; TODO: use the optional :graphs parameter
(defun create-associated-objects-vector (node &key graphs)
    (let ((vector (create-weighted-vector)))
        (visit-associated-objects node
            (lambda (obj weight)
                (set-weight vector obj weight)))
    vector))

(defun choose-random-vector-element-by-weight (vector)
    ;; TODO: the call to weighted-vector-magnitude could be avoided by assuming that the vector is normalized, or storing its magnitude
    (let ((r (- (random (+ 1.0 (weighted-vector-magnitude vector))) 1.0)) (sum 0.0))
        (loop for k being the hash-key using (hash-value v) of vector do
            (setq sum (+ sum v))
	    (if (>= sum r) (return-from choose-random-vector-element-by-weight k)))))

;; TODO: cache association weights in a 2D hash table
(defun traverse-association (source-node report-step-callback)
    (let ((objects-vector (create-associated-objects-vector source-node)))
        (let ((choice (choose-random-vector-element-by-weight objects-vector)))
	    (if (not (eq nil choice))
	        (funcall report-step-callback choice (get-weight objects-vector choice))))))



