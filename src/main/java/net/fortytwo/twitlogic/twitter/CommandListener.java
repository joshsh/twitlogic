package net.fortytwo.twitlogic.twitter;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.TwitLogicAgent;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.util.properties.PropertyException;

import java.util.logging.Logger;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 8:21:54 PM
 */
public class CommandListener implements Handler<Tweet, TweetHandlerException> {
    private static final Logger LOGGER = TwitLogic.getLogger(CommandListener.class);
    private final String userName;
    private final TwitLogicAgent agent;
    private final Handler<Tweet, TweetHandlerException> baseHandler;

    public CommandListener(final TwitLogicAgent agent,
                           final Handler<Tweet, TweetHandlerException> baseHandler) throws PropertyException {
        this.agent = agent;
        this.baseHandler = baseHandler;
        userName = TwitLogic.getConfiguration().getString(TwitLogic.TWITTER_USERNAME);
    }

    public boolean handle(final Tweet tweet) throws TweetHandlerException {
        String s = null == tweet.getInReplyToUser()
                ? null
                : tweet.getInReplyToUser().getScreenName();
        // Note: reply-to-tweet is not taken into account here
        if ((null != s && s.equals(userName))
                || tweet.getText().trim().startsWith("@" + userName)) {
            LOGGER.info("received a command from " + tweet.getUser() + ": " + tweet.getText());
            try {
                agent.interpretCommand(tweet);
            } catch (TwitterClientException e) {
                throw new TweetHandlerException(e);
            }
            System.out.println("woo");
        }

        return baseHandler.handle(tweet);
    }
}
