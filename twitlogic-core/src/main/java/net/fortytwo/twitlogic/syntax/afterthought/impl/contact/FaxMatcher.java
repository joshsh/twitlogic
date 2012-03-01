package net.fortytwo.twitlogic.syntax.afterthought.impl.contact;

import net.fortytwo.twitlogic.syntax.afterthought.DatatypePropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.Contact;

import java.util.regex.Pattern;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class FaxMatcher extends DatatypePropertyAfterthoughtMatcher {
    private static final Pattern PREDICATE = Pattern.compile("fax( number)?");

    protected String propertyURI() {
        return Contact.FAX;
    }

    protected Pattern predicatePattern() {
        return PREDICATE;
    }
}