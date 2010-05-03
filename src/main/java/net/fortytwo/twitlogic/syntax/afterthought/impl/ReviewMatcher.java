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
            HASREVIEW = new URIReference(Review.HASREVIEW),
            MAXRATING = new URIReference(Review.MAXRATING),
            MINRATING = new URIReference(Review.MINRATING),
            RATING = new URIReference(Review.RATING),
            REVIEW = new URIReference(Review.REVIEW),
            REVIEWER = new URIReference(Review.REVIEWER),
            TEXT = new URIReference(Review.TEXT),
            TYPE = new URIReference(RDF.TYPE);

    public void matchNormalized(final String normed,
                                final AfterthoughtContext context) throws MatcherException {
        if (RATING_EXPR.matcher(normed).matches()) {
            String[] pair = normed.split("/");
            int rating = Integer.valueOf(pair[0]);
            int maxRating = Integer.valueOf(pair[1]);
            // TODO: arguably 1
            int minRating = 0;

            // Only consider reviews of 4 or 5 total stars.  This avoids false
            // positives of the "(1/2)" -- first tweet of two -- variety.
            if ((4 == maxRating || 5 == maxRating)
                    && rating >= 0
                    && rating <= maxRating) {
                Resource review = context.anonymousNode();
                context.handle(new Triple(context.getSubject(), HASREVIEW, review));
                context.handle(new Triple(review, TYPE, REVIEW));
                context.handle(new Triple(review, RATING, new PlainLiteral("" + rating)));
                context.handle(new Triple(review, MAXRATING, new PlainLiteral("" + maxRating)));
                context.handle(new Triple(review, MINRATING, new PlainLiteral("" + minRating)));
                context.handle(new Triple(review, TEXT, new PlainLiteral(context.thisTweet().getText())));

                // FIXME: restore this.  Currently, it causes a transaction to hang.
                //context.handle(new Triple(review, REVIEWER, context.thisPerson()));
            }
        }
    }
}
