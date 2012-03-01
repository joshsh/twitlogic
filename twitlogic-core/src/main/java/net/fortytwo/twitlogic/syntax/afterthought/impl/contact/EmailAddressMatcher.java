package net.fortytwo.twitlogic.syntax.afterthought.impl.contact;

import net.fortytwo.twitlogic.syntax.afterthought.DatatypePropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.Contact;

import java.util.regex.Pattern;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class EmailAddressMatcher extends DatatypePropertyAfterthoughtMatcher {
    private static final Pattern PREDICATE = Pattern.compile("email( address)?");

    protected String propertyURI() {
        return Contact.EMAILADDRESS;
    }

    protected Pattern predicatePattern() {
        return PREDICATE;
    }
}