package net.fortytwo.twitlogic.syntax.afterthought;

import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Person;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.services.twitter.HandlerException;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class AfterthoughtContext implements TweetContext {
    private final TweetContext baseContext;
    private final Handler<Triple> handler;
    private final Resource subject;

    public AfterthoughtContext(final Resource subject,
                               final Handler<Triple> handler,
                               final TweetContext baseContext) {
        this.subject = subject;
        this.handler = handler;
        this.baseContext = baseContext;
    }

    public Resource getSubject() {
        return subject;
    }

    public void handle(final Triple t) throws HandlerException {
        handler.handle(t);
    }

    public void handleCompletedTriple(final Resource predicate,
                                      final Resource object) throws HandlerException {
        Triple t = new Triple(subject, predicate, object);
        handler.handle(t);
    }

    public User thisUser() {
        return baseContext.thisUser();
    }

    public Person thisPerson() {
        return thisUser().getHeldBy();
    }

    public User repliedToUser() {
        return baseContext.repliedToUser();
    }

    public User retweetedUser() {
        return baseContext.retweetedUser();
    }

    public Tweet thisTweet() {
        return baseContext.thisTweet();
    }

    public Tweet repliedToTweet() {
        return baseContext.repliedToTweet();
    }

    public Resource anonymousNode() {
        return baseContext.anonymousNode();
    }
}
