package net.fortytwo.twitlogic.syntax.afterthought;

import net.fortytwo.twitlogic.Handler;
import net.fortytwo.twitlogic.syntax.MatcherException;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.TweetContext;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Oct 1, 2009
 * Time: 6:22:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class AfterthoughtContext {
    private final TweetContext tweetContext;
    private final Handler<Triple, MatcherException> handler;
    private final Resource subject;

    public AfterthoughtContext(final Resource subject,
                               final Handler<Triple, MatcherException> handler,
                               final TweetContext tweetContext) {
        this.subject = subject;
        this.handler = handler;
        this.tweetContext = tweetContext;
    }

    public TweetContext getTweetContext() {
        return tweetContext;
    }

    public Resource getSubject() {
        return subject;
    }

    public void handleCompletedTriple(final Resource predicate,
                                      final Resource object) throws MatcherException {
        Triple t = new Triple(subject, predicate, object);
        handler.handle(t);
    }
}
