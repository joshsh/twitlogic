grammar Twiple;

@header {
package net.fortytwo.twitlogic;

import java.util.List;
import java.util.LinkedList;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.model.PlainLiteral;
import net.fortytwo.twitlogic.model.TypedLiteral;
}

@lexer::header{
package net.fortytwo.twitlogic;
}

@members {
private static final String ANYURI = "http://www.w3.org/2001/XMLSchema#anyURI";
}

/* "twiple" syntax */

tweet returns [List<List<Resource>> value]
	:	{$value = new LinkedList<List<Resource>>(); }
		((cruft)* a=allSequences {$value = $a.value;})? (cruft)* EOF
	;
	
allSequences returns [List<List<Resource>> value]
	:	c=sequence
		((cruft)+ s=allSequences {$value = $s.value;})?
		{if (null == $value) $value = new LinkedList<List<Resource>>(); $value.add(0, $c.value);}
	;

sequence returns [List<Resource> value]
	:	r=resource
		(s=sequence {$value = $s.value;})?
		{if (null == $value) $value = new LinkedList<Resource>(); $value.add(0, $r.value);}
	;

resource returns [Resource value]
	:	h=hashtag {$value = $h.value;}
	|	s=screenName {$value = $s.value;}
	|	u=url {$value = $u.value;}
	|	q=quotedString {$value = $q.value;}
	;

/* "paren" syntax */

/*
parenSyntaxSequence returns [List<Triple> value]
	:	(CRUFT)*
		st=parenStatement
		(s=parenSyntaxSequence {$value = $s.value;})?
		{if (null == $value) $value = new LinkedList<Triple>(); value.add(0, $st.value);}
	;

parenStatement returns [Statement value]
	:	s=subjectResource (cruft)*
		st=propertyObjectPair[s]
	;

propertyObjectPair[Resource s] returns [Triple value]
	:	
	;
*/
		
/* common productions */

subjectResource returns [Resource value]
	:	h=hashtag {$value = $h.value;}
	|	s=screenName {$value = $s.value;}
	// TODO: add support for URLs as subjects
	;
		
hashtag returns [Hashtag value]
	: 	HASHTAG {$value = new Hashtag($HASHTAG.text.substring(1));}
	;

screenName returns [User value]	
	:	SCREEN_NAME {$value = new User($SCREEN_NAME.text.substring(1));}
	;
	
url returns [TypedLiteral value]
	:	URL {$value = new TypedLiteral($URL.text, ANYURI);}
	;
	
quotedString returns [PlainLiteral value]
	:	QUOTED_STRING {$value = new PlainLiteral($QUOTED_STRING.text.substring(1, $QUOTED_STRING.text.length() - 1));}
	;

cruft	:	CRUFT //| PRONOUN
	;
	
WS  :		(' '|'\t'|'\n'|'\r')+ {skip();} ;

HASHTAG :	'#'('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'_')+ ;

SCREEN_NAME :	'@'('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'_')+ ;

// Note: no provision for escaped quotes
// Currently does not match single-quoted strings (e.g. 'this and that')
QUOTED_STRING :	'\"' .* '\"' ;

// Note: this is a somewhat restrictive URL regex.
// Characters not currently allowed: !$%'()*,:;<>@[\]^`{|}"
//    http://example.org/xixiluo
URL 	:	'http://'  // protocol
//URL 	:	('a'..'z')+ '://'  // protocol
        ('A'..'Z'|'a'..'z'|'0'..'9'|'-')+ ('.' ('A'..'Z'|'a'..'z'|'0'..'9'|'-')+)*  // domain name. Internationalized Domain Names are not taken into account.
        ('/' (
            ('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'_'|'#'|'&'|'+'|'.'|'/'|'='|'?'|'~')*
            ('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'/'))?)? ;  // rest (must end in a "normal" character

/*
// TODO: this is pretty ridiculous, and awkward to extend to other languages  
PRONOUN :
	|	(('I'|'i'))
	|	(('W'|'w')('E'|'e'))
	|	(('Y'|'y')('O'|'o')('U'|'u'))
	|	(('H'|'h')('E'|'e'))
	|	(('S'|'s')('H'|'h')('E'|'e'))
	|	(('I'|'i')('T'|'t'))
	|	(('T'|'t')('H'|'h')('E'|'e')('Y'|'y'))
	|	(('T'|'t')('H'|'h')('I'|'i')('S'|'s'))
	|	(('T'|'t')('H'|'h')('A'|'a')('T'|'t'))
	|	(('W'|'w')('H'|'h')('O'|'o'))
	|	(('W'|'w')('H'|'h')('I'|'i')('C'|'c')('H'|'h'))
	;
*/
	   
CRUFT 	:	'!'..'~' ;



