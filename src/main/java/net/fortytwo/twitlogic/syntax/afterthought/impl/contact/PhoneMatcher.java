package net.fortytwo.twitlogic.syntax.afterthought.impl.contact;

import net.fortytwo.twitlogic.syntax.afterthought.DatatypePropertyAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.Contact;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 29, 2009
 * Time: 10:29:09 PM
 * To change this template use File | Settings | File Templates.
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