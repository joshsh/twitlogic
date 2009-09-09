package net.fortytwo.twitlogic.twitter;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.Handler;
import net.fortytwo.twitlogic.TweetParser;
import net.fortytwo.twitlogic.model.TwitterUser;
import net.fortytwo.twitlogic.model.Triple;

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
            AUTHORIZE_URL = "http://twitter.com/oauth/authorize",
            FILTER_STREAM_URL = "http://stream.twitter.com/1/statuses/filter.json",
            SAMPLE_STREAM_URL = "http://stream.twitter.com/1/statuses/sample.json";

    private static final String
            ACCEPT = "Accept",
            USER_AGENT = "User-Agent";

    private final OAuthConsumer consumer;
    private final OAuthProvider provider;

    public TwitterSecurity() {
        String consumerKey = TwitLogic.getConfiguration().getProperty(TwitLogic.TWITTER_CONSUMER_KEY).trim();
        String consumerSecret = TwitLogic.getConfiguration().getProperty(TwitLogic.TWITTER_CONSUMER_SECRET).trim();

        consumer = new CommonsHttpOAuthConsumer(
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
        t.deriveCredentials();
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

    public void loadCredentials() {
        String accessToken = TwitLogic.getConfiguration().getProperty(TwitLogic.TWITTER_ACCESS_TOKEN).trim();
        String tokenSecret = TwitLogic.getConfiguration().getProperty(TwitLogic.TWITTER_ACCESS_TOKEN_SECRET).trim();

        // if not yet done, load the token and token secret for
        // the current user and set them
        consumer.setTokenWithSecret(accessToken, tokenSecret);
    }

    public void processSampleStream(final Handler<TwitterStatus, Exception> handler) throws Exception {
        HttpGet request = new HttpGet(SAMPLE_STREAM_URL);

        processStream(request, handler);
    }

    public void processTrackFilterStream(final String[] keywords, final Handler<TwitterStatus, Exception> handler) throws Exception {
        if (keywords.length > TwitterAPI.DEFAULT_TRACK_KEYWORDS_LIMIT) {
            throw new IllegalArgumentException("the default access level allows up to "
                    + TwitterAPI.DEFAULT_TRACK_KEYWORDS_LIMIT
                    + " track keywords (you have tried to use " + keywords.length + ")");
        }

        HttpPost request = new HttpPost(FILTER_STREAM_URL);

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("track", commaDelimit(keywords)));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        request.setEntity(entity);

        processStream(request, handler);
    }

    public void processFollowFilterStream(final String[] userIds,
                                          final Handler<TwitterStatus, Exception> handler,
                                          final int count) throws Exception {
        if (userIds.length > TwitterAPI.DEFAULT_FOLLOW_USERIDS_LIMIT) {
            throw new IllegalArgumentException("the default access level allows up to "
                    + TwitterAPI.DEFAULT_FOLLOW_USERIDS_LIMIT
                    + " follow userids (you have tried to use " + userIds.length + ")");
        }

        HttpPost request = new HttpPost(FILTER_STREAM_URL);

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("follow", commaDelimit(userIds)));
        if (count > 0) {
            formparams.add(new BasicNameValuePair("count", "" + count));
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        request.setEntity(entity);

        processStream(request, handler);
    }

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
    }

    private void showResponseInfo(final HttpResponse response) {
        System.out.println("response code: " + response.getStatusLine().getStatusCode());

        HeaderIterator iter = response.headerIterator();
        while (iter.hasNext()) {
            Header h = iter.nextHeader();
            System.out.println(h.getName() + ": " + h.getValue());
        }
    }

    private void processStream(final HttpUriRequest request, final Handler<TwitterStatus, Exception> handler) throws Exception {

        setAcceptHeader(request, new String[]{"application/json"});
        setAgent(request);

        consumer.sign(request);
        request.setHeader("Authorization", new String(Base64.encodeBase64("joshsh:escher".getBytes())));

        HttpClient client = createClient();
        HttpResponse response = client.execute(request);
        showResponseInfo(response);

        if (200 == response.getStatusLine().getStatusCode()) {
            HttpEntity responseEntity = response.getEntity();
            new StatusStreamParser(handler).parse(responseEntity.getContent());
        } else {
            response.getEntity().writeTo(System.err);
        }
    }

    private String commaDelimit(final String[] elements) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (String s : elements) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(s);
        }

        return sb.toString();
    }

    private void setAgent(final HttpRequest request) {
        request.setHeader(USER_AGENT, TwitLogic.getName() + "/" + TwitLogic.getVersion());
    }

    private HttpClient createClient() {
        HttpClient client = new DefaultHttpClient();
        //client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
        //        new DefaultHttpMethodRetryHandler());

        // Twitter streaming API requires infinite timeout
        client.getParams().setParameter("http.connection.timeout", 0);
        client.getParams().setParameter("http.socket.timeout", 0);

        return client;
    }

    private static void setAcceptHeader(final HttpRequest request, final String[] mimeTypes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mimeTypes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(mimeTypes[i]);
        }

        request.setHeader(ACCEPT, sb.toString());
    }
}
