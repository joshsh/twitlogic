(in-package :triple-store-user)
(enable-!-reader)

(register-namespace "wn" "http://www.w3.org/2006/03/wn/wn20/schema/")
(register-namespace "instances" "http://www.w3.org/2006/03/wn/wn20/instances/")

(defun visit-wordsenses-of (lexicalForm visitor)
    (iterate-cursor (t1 (get-triples :p !wn:lexicalForm :o (literal lexicalForm)))
        (iterate-cursor (t2 (get-triples :p !wn:word :o (subject t1)))
	    (funcall visitor (subject t2)))))

