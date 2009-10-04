package net.fortytwo.twitlogic.syntax.afterthought;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.syntax.MatcherException;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.TweetContext;

/**
 * User: josh
 * Date: Oct 1, 2009
 * Time: 6:22:53 PM
 */
public class AfterthoughtContext implements TweetContext {
    private final TweetContext baseContext;
    private final Handler<Triple, MatcherException> handler;
    private final Resource subject;

    public AfterthoughtContext(final Resource subject,
                               final Handler<Triple, MatcherException> handler,
                               final TweetContext baseContext) {
        this.subject = subject;
        this.handler = handler;
        this.baseContext = baseContext;
    }

    public Resource getSubject() {
        return subject;
    }

    public void handle(final Triple t) throws MatcherException {
        handler.handle(t);
    }

    public void handleCompletedTriple(final Resource predicate,
                                      final Resource object) throws MatcherException {
        Triple t = new Triple(subject, predicate, object);
        handler.handle(t);
    }

    public User thisUser() {
        return baseContext.thisUser();
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
