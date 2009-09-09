package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.twitter.TwitterStatus;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.syntax.TweetParser;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
* User: josh
* Date: Sep 8, 2009
* Time: 10:37:05 PM
* To change this template use File | Settings | File Templates.
*/
class ExampleStatusHandler implements Handler<TwitterStatus, Exception> {
    private final TweetParser parser = new TweetParser();

    public boolean handle(final TwitterStatus status) throws Exception {
        System.out.println("" + status.getUser().getScreenName() + ": " + status.getText());

        List<Triple> twiples = parser.parse(status.getText());
        //System.out.println("\t#twiples: " + twiples.size());
        if (0 < twiples.size()) {
            for (Triple t : twiples) {
                System.out.println("\t" + t);
            }
        }

        return true;
    }
}
