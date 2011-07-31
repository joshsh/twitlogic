package net.fortytwo.twitlogic.syntax.afterthought;

import net.fortytwo.twitlogic.model.PlainLiteral;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.syntax.MatcherException;

import java.util.regex.Pattern;

/**
 * User: josh
 * Date: Sep 29, 2009
 * Time: 9:14:16 PM
 */
public abstract class DatatypePropertyAfterthoughtMatcher extends AfterthoughtMatcher {

    protected abstract String propertyURI();

    protected abstract Pattern predicatePattern();

    public void matchNormalized(final String normed,
                                final AfterthoughtContext context) throws MatcherException {

        String[] a = predicatePattern().split(normed);
        //System.out.println("a.length = " + a.length);
        //System.out.println("a[0] = " + a[0]);
        
        if (2 == a.length && 0 == a[0].length()) {
            String v = a[1].trim();
            if (0 < v.length()) {
                try {
                    context.handleCompletedTriple(new URIReference(propertyURI()),
                            new PlainLiteral(v));
                } catch (HandlerException e) {
                    throw new MatcherException(e);
                }
            }
        }
    }
}