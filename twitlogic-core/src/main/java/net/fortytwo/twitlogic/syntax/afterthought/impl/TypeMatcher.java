package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.PlainLiteral;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.syntax.MatcherException;
import net.fortytwo.twitlogic.syntax.TweetSyntax;
import net.fortytwo.twitlogic.syntax.afterthought.AfterthoughtContext;
import net.fortytwo.twitlogic.syntax.afterthought.AfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.FOAF;
import net.fortytwo.twitlogic.vocabs.RDF;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TypeMatcher extends AfterthoughtMatcher {
    private static final Set<String> MALE_SYNONYMS;
    private static final Set<String> FEMALE_SYNONYMS;

    private enum Gender {
        MALE,
        FEMALE;

        public String toString() {
            return this.name().toLowerCase();
        }
    }

    static {
        MALE_SYNONYMS = new HashSet<String>();
        MALE_SYNONYMS.addAll(Arrays.asList("male", "man", "boy"));
        FEMALE_SYNONYMS = new HashSet<String>();
        FEMALE_SYNONYMS.addAll(Arrays.asList("female", "woman", "girl"));
    }

    protected void matchNormalized(final String normed,
                                   final AfterthoughtContext context) throws MatcherException {
        String l = normed.toLowerCase();

        if (l.startsWith("a ") | l.startsWith("an ")) {
            String rest = normed.substring(normed.indexOf(" ") + 1);

            if (TweetSyntax.HASHTAG_PATTERN.matcher(rest).matches()) {
                try {
                    context.handleCompletedTriple(new URIReference(RDF.TYPE), new Hashtag(rest.substring(1)));
                } catch (HandlerException e) {
                    throw new MatcherException(e);
                }
            } else {
                String rl = rest.toLowerCase();
                if (MALE_SYNONYMS.contains(rl)) {
                    produceGender(Gender.MALE, context);
                } else if (FEMALE_SYNONYMS.contains(rl)) {
                    produceGender(Gender.FEMALE, context);
                }
            }
        } else if (MALE_SYNONYMS.contains(l)) {
            produceGender(Gender.MALE, context);
        } else if (FEMALE_SYNONYMS.contains(l)) {
            produceGender(Gender.FEMALE, context);
        }
    }

    private void produceGender(final Gender gender,
                               final AfterthoughtContext context) throws MatcherException {
        try {
            context.handleCompletedTriple(new URIReference(FOAF.GENDER),
                    new PlainLiteral(gender.toString()));
        } catch (HandlerException e) {
            throw new MatcherException(e);
        }
    }
}