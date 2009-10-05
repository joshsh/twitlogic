package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.PersistenceContext;
import net.fortytwo.twitlogic.twitter.TwitterClient;
import net.fortytwo.twitlogic.twitter.TwitterClientException;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 9:14:03 PM
 */
public class TwitLogicAgent {
    private final TwitterClient twitterClient;
    private final PersistenceContext persistenceContext;
    private final String screenName;

    public TwitLogicAgent(final String screenName,
                          final TwitterClient twitterClient,
                          final PersistenceContext persistenceContext) {
        this.screenName = screenName;
        this.twitterClient = twitterClient;
        this.persistenceContext = persistenceContext;
    }

    public void interpretCommand(final Tweet request) throws TwitterClientException {
        Tweet response;

        String text = request.getText().trim();
        String s = "@" + screenName;
        if (text.startsWith(s)) {
            text = text.substring(s.length()).trim();
        }

        String link = null;
        if (TwitLogic.HASHTAG_PATTERN.matcher(text).matches()) {
            link = persistenceContext.valueOf(new Hashtag(text.substring(1)));
        } else if (TwitLogic.USERNAME_PATTERN.matcher(text).matches()) {
            link = persistenceContext.valueOf(new User(text.substring(1)));
        }

        if (null == link) {
            response = dontUnderstand(request);
        } else {
            response = replyWithLink(request, link);
        }

        /*
response = new Tweet();
response.setInReplyToTweet(request);

String query = request.getText().trim();
String s = "@" + request;

if (query.startsWith(s)) {
  query = query.substring(s.length()).trim();
}

response.setText("@" + request.getUser().getScreenName() + " ");     */

        twitterClient.updateStatus(response);
    }

    private Tweet createReply(final Tweet request,
                              final String message) {
        String text = "@" + request.getUser().getScreenName() + " " + message;
        Tweet reply = new Tweet();
        reply.setText(text);

        // Note: one or the other of these is probably sufficient...
        reply.setInReplyToTweet(request);
        reply.setInReplyToUser(request.getUser());

        return reply;
    }

    private Tweet dontUnderstand(final Tweet request) {
        String message = "sorry, I don't understand! Maybe a human like @joshsh can help you out.";
        return createReply(request, message);
    }

    private Tweet replyWithLink(final Tweet request,
                                final String link) {
        String message = "see: " + link;
        return createReply(request, message);
    }
}
