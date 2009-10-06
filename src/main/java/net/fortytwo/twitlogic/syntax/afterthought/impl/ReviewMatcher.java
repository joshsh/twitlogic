package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.model.PlainLiteral;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.syntax.MatcherException;
import net.fortytwo.twitlogic.syntax.afterthought.AfterthoughtContext;
import net.fortytwo.twitlogic.syntax.afterthought.AfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.RDF;
import net.fortytwo.twitlogic.vocabs.Review;

import java.util.regex.Pattern;

/**
 * User: josh
 * Date: Oct 1, 2009
 * Time: 1:42:38 AM
 */
public class ReviewMatcher extends AfterthoughtMatcher {
    private final static Pattern RATING_EXPR = Pattern.compile("(0|([1-9][0-9]*))/[1-9][0-9]*");

    /*
    public static void main(final String[] args) {
        System.out.println("" + RATING_EXPR.matcher("5/5").matches());    
    }*/

    private static final URIReference
            TYPE = new URIReference(RDF.TYPE),
            HASREVIEW = new URIReference(Review.HASREVIEW),
            REVIEW = new URIReference(Review.REVIEW),
            RATING = new URIReference(Review.RATING),
            TEXT = new URIReference(Review.TEXT),
            REVIEWER = new URIReference(Review.REVIEWER);

    public void matchNormalized(final String normed,
                                final AfterthoughtContext context) throws MatcherException {
        if (RATING_EXPR.matcher(normed).matches()) {
            String[] pair = normed.split("/");
            int n = Integer.valueOf(pair[0]);
            int d = Integer.valueOf(pair[1]);
            // TODO: this will give ratings of ridiculously high precision.  Perhaps use MAXRATING, MINRATING
            double rating = n > d
                    ? 1.0
                    : 5.0 * n / (double) d;

            Resource review = context.anonymousNode();
            context.handle(new Triple(context.getSubject(), HASREVIEW, review));
            context.handle(new Triple(review, TYPE, REVIEW));
            context.handle(new Triple(review, RATING, new PlainLiteral("" + rating)));
            context.handle(new Triple(review, REVIEWER, context.thisUser()));
            context.handle(new Triple(review, TEXT, new PlainLiteral(context.thisTweet().getText())));
        }
    }
}
