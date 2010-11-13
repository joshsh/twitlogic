package net.fortytwo.twitlogic.logging;

import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.services.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.flow.Handler;

/**
 * User: josh
* Date: Jul 19, 2010
* Time: 3:55:52 PM
*/
public class TweetReceivedLogger implements Handler<Tweet, TweetHandlerException> {
    private final TweetStatistics statistics;
    private final Handler<Tweet, TweetHandlerException> baseHandler;

    public TweetReceivedLogger(final TweetStatistics statistics,
                               final Handler<Tweet, TweetHandlerException> baseHandler) {
        this.statistics = statistics;
        this.baseHandler = baseHandler;
    }

    public boolean handle(final Tweet tweet) throws TweetHandlerException {
        boolean b = baseHandler.handle(tweet);
        statistics.tweetReceived(tweet);
        return b;
    }
}