grammar TwitLogic;

@header {
package net.fortytwo.twitlogic;

import java.util.List;
import java.util.LinkedList;
import net.fortytwo.twitlogic.model.TwitResource;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.TwitterUser;
import net.fortytwo.twitlogic.model.LiteralResource;
import net.fortytwo.twitlogic.model.URIBasedResource;
import net.fortytwo.twitlogic.model.TwitStatement;
}

@lexer::header{
package net.fortytwo.twitlogic;
}

@members {
}

tweet returns [List<List<TwitResource>> value]
	:	(CRUFT)* a=allSequences {$value = $a.value;} (CRUFT)*
	;
	
allSequences returns [List<List<TwitResource>> value]
	:	c=sequence
		((CRUFT)+ s=allSequences {$value = $s.value;})?
		{if (null == $value) $value = new LinkedList<List<TwitResource>>(); $value.add(0, $c.value);}
	;

sequence returns [List<TwitResource> value]
	:	r=resource
		(s=sequence {$value = $s.value;})?
		{if (null == $value) $value = new LinkedList<TwitResource>(); $value.add(0, $r.value);}
	;
	
/*
sequence returns [List<TwitResource> value]
	:	({$value = new LinkedList<TwitResource>();}| s=sequence {$value = $s.value;})
		r=resource {$value.add($r.value);}
	;
	*/
	
/*
tweet returns [List<TwitStatement> value]
	:	{$value = new LinkedList<TwitStatement>();}
		(CRUFT)* (t=twiple {$value.add($t.value);} (CRUFT)*)*
	;

twiple returns [TwitStatement value]
	:	s=subject p=predicate o=object {value = new TwitStatement($s.value, $p.value, $o.value);}
	;	 

subject returns [TwitResource value]
	:	h=hashtag {$value = $h.value;}
	|	s=screenName {$value = $s.value;}
	|	u=url {$value = $u.value;}
	;

predicate returns [TwitResource value]
	:	h=hashtag {$value = $h.value;}
	;
	
object returns [TwitResource value]
	:	h=hashtag {$value = $h.value;}
	|	s=screenName {$value = $s.value;}
	|	u=url {$value = $u.value;}
	|	q=quotedString {$value = $q.value;}
	;
*/

resource returns [TwitResource value]
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
	
url returns [URIBasedResource value]
	:	URL {$value = new URIBasedResource($URL.text);}
	;
	
quotedString returns [TwitResource value]
	:	QUOTED_STRING {$value = new URIBasedResource($QUOTED_STRING.text.substring(1, $QUOTED_STRING.text.length() - 1));}
	;
	
WS  :   (' '|'\t'|'\n'|'\r')+ {skip();} ;

HASHTAG :'#'('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'_')+ ;

SCREEN_NAME :'@'('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'_')+ ;

// Note: no provision for escaped quotes
// Currently does not match single-quoted strings (e.g. 'this and that')
QUOTED_STRING :	'\"' .* '\"' ;

// Note: this is a somewhat restrictive URL regex.
// Characters not currently allowed: !$%'()*,:;<>@[\]^`{|}"
URL 	:	('a'..'z')+ '://'  // protocol
        ('A'..'Z'|'a'..'z'|'0'..'9'|'-')+ ('.' ('A'..'Z'|'a'..'z'|'0'..'9'|'-'))*  // domain name. Internationalized Domain Names are not taken into account.
        ('/' (
            ('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'_'|'#'|'&'|'+'|'.'|'/'|'='|'?'|'~')*
            ('A'..'Z'|'a'..'z'|'0'..'9'|'-'|'/') )?)? ;  // rest (must end in a "normal" character
            
CRUFT 	:	('!'..'~')+ ;



