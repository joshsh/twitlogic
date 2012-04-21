package net.fortytwo.twitlogic.syntax.twiple;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.TwipleLexer;
import net.fortytwo.twitlogic.TwipleParser;
import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.PlainLiteral;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.model.TypedLiteral;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.syntax.Matcher;
import net.fortytwo.twitlogic.syntax.MatcherException;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import java.util.List;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TwipleMatcher implements Matcher {
    private enum PartOfSpeech {
        SUBJECT, PREDICATE, OBJECT
    }

    // Penalty weights (1 --> no penalty.  0 --> candidate will be disregarded completely).
    private static final float
            PREDICATE_HASHTAG_HAS_UNLIKELY_FIRST_CHARACTER = 0.5f,
            TRIPLE_IS_NOT_AT_HEAD_OF_SEQUENCE = 0.5f,
            TRIPLE_IS_NOT_AT_TAIL_OF_SEQUENCE = 0.5f;

    private void illegalTypeForPartOfSpeech(final Resource resource,
                                            final PartOfSpeech pos) {
        throw new IllegalArgumentException("resource " + resource
                + " of type " + resource.getType()
                + " is not allowed in the " + pos + " position of a triple");
    }

    private boolean isLegalTypeForPartOfSpeech(final Resource resource,
                                               final PartOfSpeech pos) {
        switch (resource.getType()) {
            case HASHTAG:
                return true;
            case PLAIN_LITERAL:
                return PartOfSpeech.OBJECT == pos;
            case TYPED_LITERAL:
                return PartOfSpeech.OBJECT == pos;
            case USER:
                return PartOfSpeech.PREDICATE != pos;
            default:
                throw new IllegalStateException();
        }
    }

    private float weightHashtag(final Hashtag hashtag,
                                final PartOfSpeech pos) {
        // TODO: consider penalties for long or strange hashtags, e.g. #120304ff234
        float weight = 1f;

        switch (pos) {
            case OBJECT:
                return weight;
            case PREDICATE:
                // Note: assessing candidate predicates is important, as many
                // apparent triples are simply sequences of hashtags with no
                // structure, e.g. "Dude WTF I swallowed a quarter! #im #so #wasted

                // Predicate hashtags are penalized if they don't begin with a
                // lowercase letter. Hashtags which begin with numbers and caps
                // are likely to be "proper nouns".
                char c = hashtag.getName().charAt(0);
                if (!Character.isLetter(c) || !Character.isLowerCase(c)) {
                    weight *= PREDICATE_HASHTAG_HAS_UNLIKELY_FIRST_CHARACTER;
                }

                // TODO: natural language analysis of the predicate. Hashtags which appear to contain full natural language words are more likely intended to be predicates.
                return weight;
            case SUBJECT:
                return weight;
            default:
                illegalTypeForPartOfSpeech(hashtag, pos);
                return 0;
        }
    }

    private float weightTwitterUser(final User user,
                                    final PartOfSpeech pos) {
        // TODO: consider penalties for long or strange usernames (or nonexistent ones, if it's practical to check)
        float weight = 1f;

        switch (pos) {
            case OBJECT:
                return weight;
            case SUBJECT:
                return weight;
            default:
                illegalTypeForPartOfSpeech(user, pos);
                return 0;
        }
    }

    private float weightLiteral(final PlainLiteral plainLiteral,
                                final PartOfSpeech pos) {
        // TODO: consider penalties for long or strange literals
        float weight = 1f;

        switch (pos) {
            case OBJECT:
                return weight;
            default:
                illegalTypeForPartOfSpeech(plainLiteral, pos);
                return 0;
        }
    }

    private float weightURL(final TypedLiteral uri,
                            final PartOfSpeech pos) {
        // TODO: consider penalties for long or strange URLs
        float weight = 1f;

        switch (pos) {
            case OBJECT:
                return weight;
            default:
                illegalTypeForPartOfSpeech(uri, pos);
                return 0;
        }
    }

    private float weightResource(final Resource resource,
                                 final PartOfSpeech pos) {
        switch (resource.getType()) {
            case HASHTAG:
                return weightHashtag((Hashtag) resource, pos);
            case PLAIN_LITERAL:
                return weightLiteral((PlainLiteral) resource, pos);
            case TYPED_LITERAL:
                return weightURL((TypedLiteral) resource, pos);
            case USER:
                return weightTwitterUser((User) resource, pos);
            default:
                throw new IllegalStateException("unexpected type: " + resource.getClass());
        }
    }

    /*
    private void completeSubjectPredicateAndObject(final TwitTokenSequence sequence,
                                                   final float weight,
                                                   final Handler<TwitStatement, Exception> resultHandler) {
        // Need at least three tokens to make a triple.
        if (sequence.length() < 3) {
            return;
        }

        TwitToken subj = sequence.get(0);

        switch (subj.getType()) {
            case HASHTAG:
                break;
            case SCREEN_NAME:
                break;
            default:

        }
    }

    private void completePredicateAndObject(final TwitTokenSequence sequence,
                                            final float weight,
                                            final Handler<TwitStatement, Exception> resultHandler,
                                            final TwitResource subject) {

    }

    private void completeObject(final TwitTokenSequence sequence,
                                final float weight,
                                final Handler<TwitStatement, Exception> resultHandler,
                                final TwitResource subject,
                                final TwitResource predicate) throws Exception {
        TwitToken objToken = sequence.get(0);
        TwitResource object = objToken.getResource();

        float w = weight
                * weightResource(subject, PartOfSpeech.SUBJECT)
                * weightResource(predicate, PartOfSpeech.PREDICATE)
                * weightResource(object, PartOfSpeech.OBJECT);

        produceStatement(subject, predicate, object, w, resultHandler);


    }

    private void produceStatement(final TwitResource subject,
                                  final TwitResource predicate,
                                  final TwitResource object,
                                  final float weight,
                                  final Handler<TwitStatement, Exception> resultHandler) throws Exception {
        TwitStatement st = new TwitStatement(subject, predicate, object, weight);
        resultHandler.handle(st);
    } */

    private void matchTriples(final List<Resource> sequence,
                              final Handler<Triple> resultHandler) throws MatcherException {
        for (int i = 0; i < sequence.size() - 2; i++) {
            Resource subject = sequence.get(i);
            Resource predicate = sequence.get(i + 1);
            Resource object = sequence.get(i + 2);

            if (null != subject && null != predicate && null != object
                    && isLegalTypeForPartOfSpeech(subject, PartOfSpeech.SUBJECT)
                    && isLegalTypeForPartOfSpeech(predicate, PartOfSpeech.PREDICATE)
                    && isLegalTypeForPartOfSpeech(object, PartOfSpeech.OBJECT)) {
                boolean isPrefix = 0 == i;
                boolean isSuffix = sequence.size() - 3 == i;

                float weight = 1f;
                // Note: assumes that the first item in the array is a resource.
                if (!isPrefix) {
                    weight *= TRIPLE_IS_NOT_AT_HEAD_OF_SEQUENCE;
                }
                // Note: assumes that the last item in the array is a resource.
                if (!isSuffix) {
                    weight *= TRIPLE_IS_NOT_AT_TAIL_OF_SEQUENCE;
                }

                weight *= weightResource(subject, PartOfSpeech.SUBJECT);
                weight *= weightResource(predicate, PartOfSpeech.PREDICATE);
                weight *= weightResource(object, PartOfSpeech.OBJECT);

                Triple st = new Triple(subject, predicate, object, weight);
                try {
                    if (!resultHandler.isOpen()) {
                        break;
                    }

                    resultHandler.handle(st);
                } catch (HandlerException e) {
                    throw new MatcherException(e);
                }
            }
        }
    }


    public void match(final String expression,
                      final Handler<Triple> handler,
                      final TweetContext context) throws MatcherException {
        // TODO: is it very inefficient to create a new lexer and parser for each input string?
        CharStream s = new ANTLRStringStream(expression);
        TwipleLexer lexer = new TwipleLexer(s);
        CommonTokenStream tokens = new CommonTokenStream();
        tokens.setTokenSource(lexer);
        TwipleParser parser = new TwipleParser(tokens);

        List<List<Resource>> sequences;
        try {
            sequences = parser.tweet();
        } catch (RecognitionException e) {
            throw new MatcherException(e);
        }

        for (List<Resource> sequence : sequences) {
            matchTriples(sequence, handler);
        }
    }

    /*
    public List<Triple> parse(final String text) throws Exception {
        // TODO: is it very inefficient to create a new lexer and parser for each input string?
        CharStream s = new ANTLRStringStream(text);
        TwipleLexer lexer = new TwipleLexer(s);
        CommonTokenStream tokens = new CommonTokenStream();
        tokens.setTokenSource(lexer);
        TwipleParser parser = new TwipleParser(tokens);

        final List<Triple> allStatements = new LinkedList<Triple>();

        Handler<Triple, Exception> resultHandler = new Handler<Triple, Exception>() {
            public boolean handle(final Triple t) throws Exception {
                allStatements.add(t);
                return true;
            }
        };

        for (List<Resource> sequence : parser.tweet()) {
            matchStatements(sequence, resultHandler);
        }

        Comparator<Triple> cmp = new Comparator<Triple>() {
            public int compare(final Triple first,
                               final Triple second) {
                return ((Float) second.getWeight()).compareTo(first.getWeight());
            }
        };

        // Sort in order of decreasing weight.
        Collections.sort(allStatements, cmp);

        return allStatements;
    }
    */
}
