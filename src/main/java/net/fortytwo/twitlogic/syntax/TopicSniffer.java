package net.fortytwo.twitlogic.syntax;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.model.Entities;
import net.fortytwo.twitlogic.services.twitter.TweetHandlerException;

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
        if (null == tweet.getEntities() && null != tweet.getText()) {
            Entities entities = new Entities();

            Collection<Hashtag> topics = entities.getTopics();
            for (String tag : TweetSyntax.findHashtags(tweet.getText())) {
                topics.add(new Hashtag(tag));
            }

            Collection<URIReference> links = entities.getLinks();
            for (String s : TweetSyntax.findLinks(tweet.getText())) {
                links.add(new URIReference(s));
            }

            tweet.setEntities(entities);
        }

        return baseHandler.handle(tweet);
    }
}
