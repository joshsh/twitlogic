package net.fortytwo.twitlogic.syntax.afterthought.impl.contact;

import net.fortytwo.twitlogic.syntax.afterthought.DatatypePropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.Contact;

import java.util.regex.Pattern;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class PhoneMatcher extends DatatypePropertyAfterthoughtMatcher {
    private static final Pattern PREDICATE = Pattern.compile("phone( number)?");

    protected String propertyURI() {
        return Contact.PHONE;
    }

    protected Pattern predicatePattern() {
        return PREDICATE;
    }
}