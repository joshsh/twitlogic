package net.fortytwo.twitlogic.logging;

import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.services.twitter.HandlerException;
import net.fortytwo.twitlogic.flow.Handler;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TweetReceivedLogger implements Handler<Tweet> {
    private final TweetStatistics statistics;
    private final Handler<Tweet> baseHandler;

    public TweetReceivedLogger(final TweetStatistics statistics,
                               final Handler<Tweet> baseHandler) {
        this.statistics = statistics;
        this.baseHandler = baseHandler;
    }

    public boolean handle(final Tweet tweet) throws HandlerException {
        boolean b = baseHandler.handle(tweet);
        statistics.tweetReceived(tweet);
        return b;
    }
}
