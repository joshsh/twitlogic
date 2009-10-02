package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.syntax.afterthought.AfterthoughtContext;
import net.fortytwo.twitlogic.syntax.afterthought.AfterthoughtMatcher;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Oct 1, 2009
 * Time: 1:42:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReviewRatingMatcher extends AfterthoughtMatcher {
    private final static Pattern RATING = Pattern.compile("(0|([1-9][0-9]*))/[1-9][0-9]*");

    public void matchNormalized(final String normed,
                                final AfterthoughtContext context) {
        if (RATING.matcher(normed).matches()) {
            String[] pair = normed.split("/");
            int n = Integer.valueOf(pair[0]);
            int d = Integer.valueOf(pair[1]);
            double rating = n > d
                    ? 1.0
                    : n / (double) d;

            // TODO

        }
    }
}
