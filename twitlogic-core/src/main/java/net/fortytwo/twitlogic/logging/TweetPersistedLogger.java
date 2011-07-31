package net.fortytwo.twitlogic.logging;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * User: josh
* Date: Jul 19, 2010
* Time: 3:55:27 PM
*/
public class TweetPersistedLogger implements Handler<Tweet> {
    private final TweetStatistics statistics;
    private final Handler<Tweet> baseHandler;

    public TweetPersistedLogger(final TweetStatistics statistics,
                                final Handler<Tweet> baseHandler) {
        this.statistics = statistics;
        this.baseHandler = baseHandler;
    }

    public boolean handle(final Tweet tweet) throws HandlerException {
        boolean b = baseHandler.handle(tweet);
        statistics.tweetPersisted(tweet);
        return b;
    }
}
