package net.fortytwo.twitlogic.syntax.afterthought;

import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MatcherException;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.RecognitionException;

import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class AfterthoughtMatcher implements Matcher {
    private final Logger LOGGER = TwitLogic.getLogger(AfterthoughtMatcher.class);

    // Note: should be lowercase
    private static final String[] PRONOUNS = {
            "i", "we",
            "you",
            "he", "she", "it", "they",
            "this", "that",
            "who", "which"
    };

    protected abstract void matchNormalized(String normed,
                                            AfterthoughtContext context) throws MatcherException;

    private class QuietLexer extends AfterthoughtLexer {
        public QuietLexer(final CharStream s) {
            super(s);
        }

        @Override
        public void emitErrorMessage(final String s) {
            LOGGER.finest(s);
        }
    }

    public void match(final String expression,
                      final Handler<Triple> handler,
                      final TweetContext tweetContext) throws MatcherException {
        AfterthoughtParserHelper helper = new AfterthoughtParserHelper() {
            public void handleAfterthoughtCandidate(final Resource subject,
                                                    final String predicateValueExpression) {
                AfterthoughtContext context = new AfterthoughtContext(subject, handler, tweetContext);
                try {
                    matchNormalized(normalize(predicateValueExpression), context);
                } catch (MatcherException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        };

        // TODO: is it very inefficient to create a new lexer and parser for each input string?
        CharStream s = new ANTLRStringStream(expression);
        Lexer lexer = new QuietLexer(s);
        CommonTokenStream tokens = new CommonTokenStream();
        tokens.setTokenSource(lexer);
        AfterthoughtParser parser = new AfterthoughtParser(tokens);
        parser.setHelper(helper);

        try {
            //System.out.println("expression: " + expression);
            parser.tweet();
        } catch (RecognitionException e) {
            throw new MatcherException(e);
        }
    }
    
    private String normalize(final String expression) {
        String s = expression.trim();
        s = s.replaceAll("[\\s]+", " ");
        s = removeUpToOneLeadingPronoun(s);

        return s;
    }

    private String removeUpToOneLeadingPronoun(final String s) {
        String sLower = s.toLowerCase();

        for (String pronoun : PRONOUNS) {
            if (sLower.startsWith(pronoun)) {
                return s.substring(pronoun.length()).trim();
            }
        }

        return s;
    }
}
