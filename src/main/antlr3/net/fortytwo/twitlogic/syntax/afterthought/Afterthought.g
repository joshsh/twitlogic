grammar Afterthought;

@header {
package net.fortytwo.twitlogic.syntax.afterthought;

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
package net.fortytwo.twitlogic.syntax.afterthought;
}

@members {
private static final String ANYURI = "http://www.w3.org/2001/XMLSchema#anyURI";
private AfterthoughtParserHelper helper;
public void setHelper(final AfterthoughtParserHelper helper) {
    this.helper = helper;
}
}

tweet
	:	candidate (nonstarter)* EOF
	;

candidate
	:	(nonstarter)* (r=subjectResource (s=PAREN_BLOCK
			{
				if (null != this.helper) {
					Resource subject = $r.value;
					String expression = $PAREN_BLOCK.text;
					expression = expression.substring(1, expression.length() - 1);
					this.helper.handleAfterthoughtCandidate(subject, expression);
				}
			})? candidate)?
	;

nonstarter
	:	CRUFT
	|	QUOTED_STRING
	|	PAREN_BLOCK
	|	URL
	;
		
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

WS 
	:	(' '|'\t'|'\n'|'\r')+ {skip();} ;

HASHTAG
	:	'#'('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'_')+ ;

SCREEN_NAME
	:	'@'('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'_')+ ;

// Note: this is a somewhat restrictive URL regex.
// Characters not currently allowed: !$%'()*,:;<>@[\]^`{|}"
//    http://example.org/xixiluo
URL
	:	'http://'  // protocol
//URL 	:	('a'..'z')+ '://'  // protocol
        ('A'..'Z'|'a'..'z'|'0'..'9'|'-')+ ('.' ('A'..'Z'|'a'..'z'|'0'..'9'|'-')+)*  // domain name. Internationalized Domain Names are not taken into account.
        ('/' (
            ('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'_'|'#'|'&'|'+'|'.'|'/'|'='|'?'|'~')*
            ('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'/'))?)? ;  // rest (must end in a "normal" character

// Note: no provision for escaped quotes
// Currently does not match single-quoted strings (e.g. 'this and that')
QUOTED_STRING options { greedy = false; }
	:	'\"' (.)* '\"' ;

PAREN_BLOCK options { greedy = false; } 
	:	'(' (.)* ')' ;

// Anything not matched by previous rules.	   
CRUFT
	:	('\u0000'..'\uFFFF') ;

