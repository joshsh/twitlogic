package net.fortytwo.twitlogic.syntax;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.persistence.SimpleTweetContext;
import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TweetAnnotator implements Handler<Tweet> {
    private final Handler<Tweet> baseHandler;
    private final Matcher matcher;

    public TweetAnnotator(final Matcher matcher,
                          final Handler<Tweet> baseHandler) {
        this.matcher = matcher;
        this.baseHandler = baseHandler;
    }

    public boolean handle(final Tweet tweet) throws HandlerException {
        Handler<Triple> tripleHandler = new Handler<Triple>() {
            public boolean handle(final Triple triple) throws HandlerException {
                //System.out.println("got an annotation: " + triple);
                tweet.getAnnotations().add(triple);
                return true;
            }
        };

        try {
            matcher.match(tweet.getText(), tripleHandler, new SimpleTweetContext(tweet));
        } catch (MatcherException e) {
            throw new HandlerException(e);
        }

        return baseHandler.handle(tweet);
    }
}
