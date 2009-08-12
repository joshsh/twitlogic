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

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 10, 2009
 * Time: 5:38:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestStuff {
    public static void main(final String[] args) throws Exception {
        new TestStuff().oauthTwitterExample();
        /*
// create a consumer object and configure it with the access
        // token and token secret obtained from the service provider
        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
                CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
        consumer.setTokenWithSecret(ACCESS_TOKEN, TOKEN_SECRET);

        // create an HTTP request to a protected resource
        HttpGet request = new HttpGet("http://example.com/protected");

        // sign the request
        consumer.sign(request);

        // send the request
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);
        */
    }

    public void oauthTwitterExample() throws OAuthCommunicationException, OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException {
        OAuthConsumer consumer = new DefaultOAuthConsumer(
                // FIXME: don't check this in
                "Uar2KZtFeVhcdb2XJGIfQ",
                // FIXME: don't check this in
                "fo5MtTSNqRmTK4JynqayiuNNfo6eToADjzTp8dRhJU",
                SignatureMethod.HMAC_SHA1);

        OAuthProvider provider = new DefaultOAuthProvider(
                consumer,
                "http://twitter.com/oauth/request_token",
                "http://twitter.com/oauth/access_token",
                "http://twitter.com/oauth/authorize");

        /****************************************************
         * The following steps should only be performed ONCE
         ***************************************************/

        // we do not support callbacks, thus pass OOB
        String authUrl = provider.retrieveRequestToken(OAuth.OUT_OF_BAND);

        // bring the user to authUrl, e.g. open a web browser and note the PIN code
        // ...

        String pinCode = // ... you have to ask this from the user, or obtain it
                // from the callback if you didn't do an out of band request

                // user must have granted authorization at this point
                provider.retrieveAccessToken(pinCode);

        // store consumer.getToken() and consumer.getTokenSecret(),
        // for the current user, e.g. in a relational database
        // or a flat file
        // ...

        /****************************************************
         * The following steps are performed everytime you
         * send a request accessing a resource on Twitter
         ***************************************************/

        // if not yet done, load the token and token secret for
        // the current user and set them
        consumer.setTokenWithSecret(ACCESS_TOKEN, TOKEN_SECRET);

        // create a request that requires authentication
        URL url = new URL("http://twitter.com/statuses/mentions.xml");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();

        // sign the request
        consumer.sign(request);

        // send the request
        request.connect();

        // response status should be 200 OK
        int statusCode = request.getResponseCode();
    }
}
