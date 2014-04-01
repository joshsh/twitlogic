package net.fortytwo.twitlogic.services.twitter;

import net.fortytwo.twitlogic.TwitLogic;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class TwitterCredentials {
    private static final Logger LOGGER = TwitLogic.getLogger(TwitterCredentials.class);

    private final OAuthConsumer consumer;
    private final OAuthProvider provider;

    private final String userName;
    private final String password;

    private final String consumerKey;
    private final String consumerSecret;

    private final String accessToken;
    private final String tokenSecret;

    public static void main(final String[] args) throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream("/tmp/twitlogic.properties"));
        TwitLogic.setConfiguration(props);

        TwitterCredentials c = new TwitterCredentials();
        c.deriveCredentials();
    }

    public TwitterCredentials() throws TwitterClientException {
        consumerKey = TwitLogic.getConfiguration().getProperty(TwitLogic.TWITTER_CONSUMER_KEY, null);
        consumerSecret = TwitLogic.getConfiguration().getProperty(TwitLogic.TWITTER_CONSUMER_SECRET, null);

        if (null == consumerKey || null == consumerSecret) {
            throw new TwitterClientException("missing OAuth credentials");
        }

        consumer = new CommonsHttpOAuthConsumer(
                consumerKey,
                consumerSecret);
        provider = new DefaultOAuthProvider(
                TwitterAPI.OAUTH_REQUEST_TOKEN_URL,
                TwitterAPI.OAUTH_ACCESS_TOKEN_URL,
                TwitterAPI.OAUTH_AUTHORIZE_URL);
        userName = null;
        password = null;

        accessToken = TwitLogic.getConfiguration().getProperty(TwitLogic.TWITTER_ACCESS_TOKEN).trim();
        tokenSecret = TwitLogic.getConfiguration().getProperty(TwitLogic.TWITTER_ACCESS_TOKEN_SECRET).trim();

        // if not yet done, load the token and token secret for
        // the current user and set them
        consumer.setTokenWithSecret(accessToken, tokenSecret);
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void sign(final HttpUriRequest request) throws OAuthExpectationFailedException, OAuthMessageSignerException, OAuthCommunicationException {
        consumer.sign(request);
    }

    private void showInfo() {
        System.out.println("consumer.getToken() = " + consumer.getToken());
        System.out.println("consumer.getTokenSecret() = " + consumer.getTokenSecret());
        System.out.println("consumer.getConsumerKey() = " + consumer.getConsumerKey());
        System.out.println("consumer.getConsumerSecret() = " + consumer.getConsumerSecret());
        System.out.println("provider.getAccessTokenEndpointUrl() = " + provider.getAccessTokenEndpointUrl());
        System.out.println("provider.getAuthorizationWebsiteUrl() = " + provider.getAuthorizationWebsiteUrl());
        System.out.println("provider.getRequestTokenEndpointUrl() = " + provider.getRequestTokenEndpointUrl());
    }

    /**
     * This should only be done once, to derive OAuth credentials.
     */
    public void deriveCredentials() throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException {
        // we do not support callbacks, thus pass OOB
        String authURL = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
        String pinCode = findPinCode(authURL);

        // user must have granted authorization at this point
        provider.retrieveAccessToken(consumer, pinCode);

        // store consumer.getToken() and consumer.getTokenSecret(),
        // for the current user, e.g. in a relational database
        // or a flat file
        showInfo();
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * bring the user to authUrl, e.g. open a web browser and note the PIN code
     * ... you have to ask this from the user, or obtain it
     * from the callback if you didn't do an out of band request
     *
     * @param authURL
     * @return
     * @throws java.io.IOException
     */
    private String findPinCode(final String authURL) throws IOException {
        /*
        JFrame frame = new JFrame("hidden frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String pinCode = (String) JOptionPane.showInputDialog(
                frame,
                "To allow " + TwitLogic.getName() + " access to your Twitter account:\n" +
                        "1) visit the following URL\n" +
                        "\t " + authURL + "\n" +
                        "2) click \"Allow\"\n" +
                        "3) enter the PIN code into the field below",
                "Twitter OAuth PIN code required",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
        frame.dispose();
        */

        System.out.println("To allow " + TwitLogic.getName() + " access to your Twitter account:\n" +
                "1) visit the following URL\n" +
                "\t " + authURL + "\n" +
                "2) click \"Allow\"\n" +
                "3) enter the PIN code below and hit ENTER");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String pinCode = br.readLine();

        return pinCode.trim();
    }

    /*
    // The following steps are performed everytime you
    // send a request accessing a resource on Twitter
    private void makeRequest() throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException {
        // create a request that requires authentication
        HttpGet request = new HttpGet("http://twitter.com/statuses/mentions.xml");
        //URL url = new URL("http://twitter.com/statuses/mentions.xml");
        //HttpURLConnection request = (HttpURLConnection) url.openConnection();

        // sign the request
        consumer.sign(request);

        // send the request
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(request);
        //request.connect();

        // response status should be 200 OK
        //int statusCode = request.getResponseCode();
        int statusCode = response.getStatusLine().getStatusCode();

        System.out.println("got status code: " + statusCode);
    }*/
}
