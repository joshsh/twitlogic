package net.fortytwo.twitlogic.logging;

import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.services.twitter.TweetHandlerException;
import net.fortytwo.twitlogic.flow.Handler;

/**
 * User: josh
* Date: Jul 19, 2010
* Time: 3:55:27 PM
*/
public class TweetPersistedLogger implements Handler<Tweet, TweetHandlerException> {
    private final TweetStatistics statistics;
    private final Handler<Tweet, TweetHandlerException> baseHandler;

    public TweetPersistedLogger(final TweetStatistics statistics,
                                final Handler<Tweet, TweetHandlerException> baseHandler) {
        this.statistics = statistics;
        this.baseHandler = baseHandler;
    }

    public boolean handle(final Tweet tweet) throws TweetHandlerException {
        boolean b = baseHandler.handle(tweet);
        statistics.tweetPersisted(tweet);
        return b;
    }
}
