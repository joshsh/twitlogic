package net.fortytwo.twitlogic.twitter;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.TwitLogicAgent;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.util.properties.PropertyException;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 8:21:54 PM
 */
public class CommandListener implements Handler<Tweet, TweetHandlerException> {
    private static final Logger LOGGER = TwitLogic.getLogger(CommandListener.class);
    //private final String userName;
    private final TwitLogicAgent agent;
    private final Handler<Tweet, TweetHandlerException> baseHandler;
    private final Set<String> selfScreenNames;

    public CommandListener(final TwitLogicAgent agent,
                           final Handler<Tweet, TweetHandlerException> baseHandler) throws PropertyException {
        this.agent = agent;
        this.baseHandler = baseHandler;

        selfScreenNames = new HashSet<String>();
        selfScreenNames.add(TwitLogic.getConfiguration().getString(TwitLogic.TWITTER_USERNAME));
        selfScreenNames.add(TwitLogicAgent.ASPIRATIONAL_SCREENNAME);
    }

    public boolean handle(final Tweet tweet) throws TweetHandlerException {
        if (null != getReplyTo(tweet)) {
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

    private String getReplyTo(final Tweet tweet) {
        if (null != tweet.getInReplyToTweet()) {
            String s = tweet.getInReplyToTweet().getUser().getScreenName();
            if (null != s && selfScreenNames.contains(s)) {
                return s;
            } else {
                return null;
            }
        } else {
            String text = tweet.getText();
            if (null == text) {
                return null;
            } else {
                text = text.trim();

                if (text.startsWith("@")) {
                    text = text.substring(1);
                    for (String s : selfScreenNames) {
                        if (text.startsWith(s)) {
                            // TODO: rule out screen names for which this screen name is a prefix
                            return s;
                        }
                    }
                }
            }
        }

        return null;
    }
}
