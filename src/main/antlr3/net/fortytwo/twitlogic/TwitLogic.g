grammar TwitLogic;

@header {
package net.fortytwo.twitlogic;

import java.util.List;
import java.util.LinkedList;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.TwitterUser;
import net.fortytwo.twitlogic.model.PlainLiteral;
import net.fortytwo.twitlogic.model.URILiteral;
}

@lexer::header{
package net.fortytwo.twitlogic;
}

@members {
}

tweet returns [List<List<Resource>> value]
	:	(CRUFT)* a=allSequences {$value = $a.value;} (CRUFT)*
	;
	
allSequences returns [List<List<Resource>> value]
	:	c=sequence
		((CRUFT)+ s=allSequences {$value = $s.value;})?
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
	
hashtag returns [Hashtag value]
	: 	HASHTAG {$value = new Hashtag($HASHTAG.text.substring(1));}
	;

screenName returns [TwitterUser value]	
	:	SCREEN_NAME {$value = new TwitterUser($SCREEN_NAME.text.substring(1));}
	;
	
url returns [URILiteral value]
	:	URL {$value = new URILiteral($URL.text);}
	;
	
quotedString returns [PlainLiteral value]
	:	QUOTED_STRING {$value = new PlainLiteral($QUOTED_STRING.text.substring(1, $QUOTED_STRING.text.length() - 1));}
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
URL 	:	('a'..'z')+ '://'  // protocol
        ('A'..'Z'|'a'..'z'|'0'..'9'|'-')+ ('.' ('A'..'Z'|'a'..'z'|'0'..'9'|'-')+)*  // domain name. Internationalized Domain Names are not taken into account.
        ('/' (
            ('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'_'|'#'|'&'|'+'|'.'|'/'|'='|'?'|'~')*
            ('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'/'))?)? ;  // rest (must end in a "normal" character
            
CRUFT 	:	'!'..'~' ;



