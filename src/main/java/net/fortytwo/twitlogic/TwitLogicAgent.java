package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.persistence.PersistenceContext;
import net.fortytwo.twitlogic.services.bitly.BitlyClient;
import net.fortytwo.twitlogic.services.bitly.BitlyClientException;
import net.fortytwo.twitlogic.twitter.TwitterClient;
import net.fortytwo.twitlogic.twitter.TwitterClientException;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 9:14:03 PM
 */
public class TwitLogicAgent {
    // TwitLogic doesn't actually own this screen name, but wants to, as it
    // is likely that users will mistakenly tweet at it.
    public final static String ASPIRATIONAL_SCREENNAME = "twitlogic";

    private final TwitterClient twitterClient;
    private final BitlyClient bitlyClient;

    public TwitLogicAgent(final TwitterClient twitterClient) throws BitlyClientException {
        this.twitterClient = twitterClient;
        bitlyClient = new BitlyClient();
    }

    public void interpretCommand(final Tweet request) throws TwitterClientException {
        Tweet response;

        String text = request.getText().trim();

        // TODO: a space character is not the only possibility for terminating the @username
        text = text.substring(text.indexOf(" ") + 1);
        /*
        String s = "@" + screenName;
        if (text.startsWith(s)) {
            text = text.substring(s.length()).trim();
        } else if (text.startsWith(ASPIRATIONAL_SCREENNAME)) {
            text = text.substring(ASPIRATIONAL_SCREENNAME.length()).trim();
        }*/

        String link = null;
        if (TwitLogic.HASHTAG_PATTERN.matcher(text).matches()) {
            link = PersistenceContext.uriOf(new Hashtag(text.substring(1)));
        } else if (TwitLogic.USERNAME_PATTERN.matcher(text).matches()) {
            link = PersistenceContext.uriOf(new User(text.substring(1)).getHeldBy());
        }

        if (null == link) {
            response = dontUnderstand(request);
        } else {
            try {
                response = replyWithLink(request, link);
            } catch (BitlyClientException e) {
                throw new TwitterClientException(e);
            }
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

    private String zitgistify(final String resourceURL) throws BitlyClientException {
        return "http://dataviewer.zitgist.com/?uri="
                + BitlyClient.percentEncode(resourceURL);
    }
    private Tweet createReply(final Tweet request,
                              final String message) {
        String text = "@" + request.getUser().getScreenName() + " " + message;
        Tweet reply = new Tweet();
        reply.setText(text);

        reply.setInReplyToTweet(request);

        return reply;
    }

    private Tweet dontUnderstand(final Tweet request) {
        String message = "sorry, I don't understand! Maybe a human like @joshsh can help you out.";
        return createReply(request, message);
    }

    private Tweet replyWithLink(final Tweet request,
                                final String resourceURL) throws BitlyClientException {
        String shortUrl = bitlyClient.shorten(
                zitgistify(resourceURL));
        String message = "see: " + shortUrl;
        return createReply(request, message);
    }
}
