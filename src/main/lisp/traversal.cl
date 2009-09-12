
(defun traverse (r visitor &rest mappings)
    (funcall (apply 'compose mappings) r visitor))

(defun >> (predicate)
    (lambda (subject visitor)
        (iterate-cursor (tr (get-triples :s subject :p predicate))
	    (funcall visitor (object tr)))))

(defun << (predicate)
    (lambda (object visitor)
        (iterate-cursor (tr (get-triples :o object :p predicate))
	    (funcall visitor (subject tr)))))

(defun compose (&rest mappings)
    (lambda (r visitor)
        (if (eq () mappings)
	    (funcall visitor r)
	    (funcall (first mappings)
	        r
		(lambda (s)
		    (funcall (apply 'compose (rest mappings)) s visitor))))))

(defun star (&rest mappings)
    (let ((mapping (apply 'compose mappings)))
        (lambda (r visitor)
            (funcall visitor r)
            (funcall mapping r
	        (lambda (s) (funcall (funcall 'star mapping) s visitor))))))

(defun optional (&rest mappings)
    (let ((mapping (apply 'compose mappings)))
        (lambda (r visitor)
            (funcall visitor r)
            (funcall mapping r visitor))))

;; apply each mapping in turn -- UNTESTED
(defun each (&rest mappings)
    (lambda (r visitor)
        (loop for m in mappings do
            (funcall m r visitor))))

(defun distinct ()
    (let ((ht (make-upi-hash-table)))
        (lambda (r visitor)
	    (when (not (gethash r ht))
	        (setf (gethash r ht) t)
		(funcall visitor r)))))

(defun filter (criterion)
    (lambda (r visitor)
        (when (funcall criterion r)
	    (funcall visitor r))))

(defun filter-require (criterion &rest mappings)
    (let ((mapping (apply 'compose mappings)))
        (lambda (r visitor)
            (let ((matched nil))
                (funcall mapping r
                    ;; TODO: save cycles by breaking out on the first match
	            (lambda (s) (when (funcall criterion s) (setf matched t))))
	        (when matched
	            (funcall visitor r))))))
