package net.fortytwo.twitlogic;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 10, 2009
 * Time: 5:38:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class TwitterSecurity {
    private static final String
            REQUEST_TOKEN_URL = "http://twitter.com/oauth/request_token",
            ACCESS_TOKEN_URL = "http://twitter.com/oauth/access_token",
            AUTHORIZE_URL = "http://twitter.com/oauth/authorize";

    private final OAuthConsumer consumer;
    private final OAuthProvider provider;

    public TwitterSecurity() {
        String consumerKey = TwitLogic.getConfiguration().getProperty(TwitLogic.TWITTER_CONSUMER_KEY).trim();
        String consumerSecret = TwitLogic.getConfiguration().getProperty(TwitLogic.TWITTER_CONSUMER_SECRET).trim();

        consumer = new DefaultOAuthConsumer(
                consumerKey,
                consumerSecret,
                SignatureMethod.HMAC_SHA1);
        provider = new DefaultOAuthProvider(
                consumer,
                REQUEST_TOKEN_URL,
                ACCESS_TOKEN_URL,
                AUTHORIZE_URL);
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

    public static void main(final String[] args) throws Exception {
        TwitterSecurity t = new TwitterSecurity();

        //t.deriveCredentials();
        t.loadCredentials();
        
//        t.showInfo();

        t.makeRequest();
    }

    /**
     * This should only be done once, to derive OAuth credentials.
     */
    public void deriveCredentials() throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException {
        // we do not support callbacks, thus pass OOB
        String authURL = provider.retrieveRequestToken(OAuth.OUT_OF_BAND);
        String pinCode = findPinCode(authURL);

        // user must have granted authorization at this point
        provider.retrieveAccessToken(pinCode);

        // store consumer.getToken() and consumer.getTokenSecret(),
        // for the current user, e.g. in a relational database
        // or a flat file
        showInfo();
    }

    /**
     * bring the user to authUrl, e.g. open a web browser and note the PIN code
     * ... you have to ask this from the user, or obtain it
     * from the callback if you didn't do an out of band request
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

    private void loadCredentials() {
        String accessToken = TwitLogic.getConfiguration().getProperty(TwitLogic.TWITTER_ACCESS_TOKEN).trim();
        String tokenSecret = TwitLogic.getConfiguration().getProperty(TwitLogic.TWITTER_ACCESS_TOKEN_SECRET).trim();

        // if not yet done, load the token and token secret for
        // the current user and set them
        consumer.setTokenWithSecret(accessToken, tokenSecret);
    }

    // The following steps are performed everytime you
    // send a request accessing a resource on Twitter
    private void makeRequest() throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException, IOException {
        // create a request that requires authentication
        URL url = new URL("http://twitter.com/statuses/mentions.xml");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();

        // sign the request
        consumer.sign(request);

        // send the request
        request.connect();

        // response status should be 200 OK
        int statusCode = request.getResponseCode();

        System.out.println("got status code: " + statusCode);
    }
}
