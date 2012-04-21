package net.fortytwo.twitlogic.syntax;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Entities;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.services.twitter.HandlerException;

import java.util.Collection;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TopicSniffer implements Handler<Tweet> {
    private final Handler<Tweet> baseHandler;

    public TopicSniffer(final Handler<Tweet> baseHandler) {
        this.baseHandler = baseHandler;
    }

    public boolean isOpen() {
        return baseHandler.isOpen();
    }

    public void handle(final Tweet tweet) throws HandlerException {
        if (null == tweet.getEntities() && null != tweet.getText()) {
            Entities entities = new Entities();

            Collection<Resource> topics = entities.getTopics();
            for (String tag : TweetSyntax.findHashtags(tweet.getText())) {
                topics.add(new Hashtag(tag));
            }

            // Note: dollar tags are not "sniffed" here.

            Collection<URIReference> links = entities.getLinks();
            for (String s : TweetSyntax.findLinks(tweet.getText())) {
                links.add(new URIReference(s));
            }

            tweet.setEntities(entities);
        }

        baseHandler.handle(tweet);
    }
}
