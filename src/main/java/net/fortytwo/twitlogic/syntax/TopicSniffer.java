package net.fortytwo.twitlogic.syntax;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.twitter.TweetHandlerException;

import java.util.Collection;

/**
 * User: josh
 * Date: Apr 5, 2010
 * Time: 6:08:07 PM
 */
public class TopicSniffer implements Handler<Tweet, TweetHandlerException> {
    private final Handler<Tweet, TweetHandlerException> baseHandler;

    public TopicSniffer(final Handler<Tweet, TweetHandlerException> baseHandler) {
        this.baseHandler = baseHandler;
    }

    public boolean handle(final Tweet tweet) throws TweetHandlerException {
        if (null != tweet.getText()) {
            Collection<Hashtag> topics = tweet.getTopics();
            for (String tag : TweetSyntax.findHashtags(tweet.getText())) {
                topics.add(new Hashtag(tag));
            }
        }

        return baseHandler.handle(tweet);
    }
}
