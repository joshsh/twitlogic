package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.model.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 30, 2009
 * Time: 1:46:56 AM
 * To change this template use File | Settings | File Templates.
 */
public interface TweetContext {
    User thisUser();
    Resource thisTweet();
}
