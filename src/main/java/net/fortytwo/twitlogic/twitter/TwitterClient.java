package net.fortytwo.twitlogic.twitter;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Oct 2, 2009
 * Time: 2:54:04 PM
 */
// TODO: keep track of # of requests, for reasons of rate-limiting
public class TwitterClient {
    private static final Logger LOGGER = TwitLogic.getLogger(TwitterClient.class);

    private static final String
            ACCEPT = "Accept",
            USER_AGENT = "User-Agent";

    private final TwitterCredentials credentials;

    public TwitterClient() {
        credentials = new TwitterCredentials();
        credentials.loadCredentials();
    }

    public void requestUserTimeline(final User user,
                                    final Handler<Tweet, Exception> handler) throws Exception {
        StringBuilder sb = new StringBuilder(TwitterAPI.STATUSES_USERTIMELINE_URL)
                .append(".json").append("?");

        if (null == user.getId()) {
            sb.append(TwitterAPI.SCREENNAME).append("=").append(user.getScreenName());
        } else {
            sb.append(TwitterAPI.USER_ID).append("=").append(user.getId());            
        }

        HttpGet request = new HttpGet(sb.toString());

        requestStatusArray(request, handler);
    }

    public void processSampleStream(final Handler<Tweet, TweetHandlerException> handler) throws Exception {
        HttpGet request = new HttpGet(TwitterAPI.SAMPLE_STREAM_URL);

        requestStatusStream(request, handler);
    }

    public void processTrackFilterStream(final String[] keywords, final Handler<Tweet, TweetHandlerException> handler) throws Exception {
        if (keywords.length > TwitterAPI.DEFAULT_TRACK_KEYWORDS_LIMIT) {
            throw new IllegalArgumentException("the default access level allows up to "
                    + TwitterAPI.DEFAULT_TRACK_KEYWORDS_LIMIT
                    + " track keywords (you have tried to use " + keywords.length + ")");
        }

        HttpPost request = new HttpPost(TwitterAPI.FILTER_STREAM_URL);

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("track", commaDelimit(keywords)));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        request.setEntity(entity);

        requestStatusStream(request, handler);
    }

    public void processFollowFilterStream(final String[] userIds,
                                          final Handler<Tweet, TweetHandlerException> handler,
                                          final int count) throws Exception {
        if (userIds.length > TwitterAPI.DEFAULT_FOLLOW_USERIDS_LIMIT) {
            throw new IllegalArgumentException("the default access level allows up to "
                    + TwitterAPI.DEFAULT_FOLLOW_USERIDS_LIMIT
                    + " follow userids (you have tried to use " + userIds.length + ")");
        }

        HttpPost request = new HttpPost(TwitterAPI.FILTER_STREAM_URL);

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("follow", commaDelimit(userIds)));
        if (count > 0) {
            formparams.add(new BasicNameValuePair("count", "" + count));
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        request.setEntity(entity);

        requestStatusStream(request, handler);
    }

    ////////////////////////////////////////////////////////////////////////////

    private void logRequest(final HttpUriRequest request) {
        LOGGER.fine("issuing request for:  " + request.getURI());
    }

    private HttpResponse prepareRequest(final HttpUriRequest request,
                                        final boolean openEnded) throws OAuthExpectationFailedException, OAuthMessageSignerException, IOException {
        logRequest(request);

        setAcceptHeader(request, new String[]{"application/json"});
        setAgent(request);

        credentials.sign(request);

        HttpClient client = createClient(openEnded);
        HttpResponse response = client.execute(request);
        showResponseInfo(response);

        if (200 != response.getStatusLine().getStatusCode()) {
            response.getEntity().writeTo(System.err);
            return null;
        } else {
            return response;
        }
    }

    private void requestStatusArray(final HttpUriRequest request,
                                    final Handler<Tweet, Exception> handler) throws Exception {
        HttpResponse response = prepareRequest(request, false);
        if (null != response) {
            HttpEntity responseEntity = response.getEntity();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            responseEntity.writeTo(bos);
            JSONArray array = new JSONArray(bos.toString());
            int length = array.length();
            for (int i = 0; i < length; i++) {
                JSONObject obj = (JSONObject) array.get(i);
                if (!handler.handle(new Tweet(obj))) {
                    break;
                }
            }
        }
    }

    private void requestStatusStream(final HttpUriRequest request,
                                     final Handler<Tweet, TweetHandlerException> handler) throws Exception {
        HttpResponse response = prepareRequest(request, true);
        if (null != response) {
            HttpEntity responseEntity = response.getEntity();
            new StatusStreamParser(handler).parse(responseEntity.getContent());
        }
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


    private void setAgent(final HttpRequest request) {
        request.setHeader(USER_AGENT, TwitLogic.getName() + "/" + TwitLogic.getVersion());
    }

    private void showResponseInfo(final HttpResponse response) {
        System.out.println("response code: " + response.getStatusLine().getStatusCode());

        HeaderIterator iter = response.headerIterator();
        while (iter.hasNext()) {
            Header h = iter.nextHeader();
            System.out.println(h.getName() + ": " + h.getValue());
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

    private HttpClient createClient(final boolean openEnded) {
        HttpClient client = new DefaultHttpClient();
        //client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
        //        new DefaultHttpMethodRetryHandler());

        // Twitter streaming API requires infinite timeout
        if (openEnded) {
            LOGGER.fine("using infinite timeout (for open-ended requests)");
            client.getParams().setParameter("http.connection.timeout", 0);
            client.getParams().setParameter("http.socket.timeout", 0);
        }

        return client;
    }
}
