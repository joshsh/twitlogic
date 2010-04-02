package net.fortytwo.twitlogic.syntax;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.persistence.SimpleTweetContext;
import net.fortytwo.twitlogic.twitter.TweetHandlerException;

/**
 * User: josh
 * Date: Apr 1, 2010
 * Time: 6:58:27 PM
 */
public class TweetAnnotator implements Handler<Tweet, TweetHandlerException> {
    private final Matcher matcher;
    private final Handler<Tweet, TweetHandlerException> baseHandler;

    public TweetAnnotator(final Matcher matcher,
                          final Handler<Tweet, TweetHandlerException> baseHandler) {
        this.matcher = matcher;
        this.baseHandler = baseHandler;
    }

    public boolean handle(final Tweet tweet) throws TweetHandlerException {
        Handler<Triple, MatcherException> tripleHandler = new Handler<Triple, MatcherException>() {
            public boolean handle(final Triple triple) throws MatcherException {
                //System.out.println("got an annotation: " + triple);
                tweet.getAnnotations().add(triple);
                return true;
            }
        };

        try {
            matcher.match(tweet.getText(), tripleHandler, new SimpleTweetContext(tweet));
        } catch (MatcherException e) {
            throw new TweetHandlerException(e);
        }

        return baseHandler.handle(tweet);
    }
}
