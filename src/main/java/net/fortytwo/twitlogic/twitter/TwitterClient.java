package net.fortytwo.twitlogic.twitter;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import org.apache.commons.httpclient.params.HttpMethodParams;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
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
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");

        credentials = new TwitterCredentials();
        credentials.loadCredentials();
    }

    public void requestUserTimeline(final User user,
                                    final Handler<Tweet, Exception> handler) throws TwitterClientException {
        StringBuilder sb = new StringBuilder(TwitterAPI.STATUSES_USER_TIMELINE_URL)
                .append(".json").append("?");

        if (null == user.getId()) {
            sb.append(TwitterAPI.SCREENNAME).append("=").append(user.getScreenName());
        } else {
            sb.append(TwitterAPI.USER_ID).append("=").append(user.getId());
        }

        HttpGet request = new HttpGet(sb.toString());

        requestStatusArray(request, handler);
    }

    public void processSampleStream(final Handler<Tweet, TweetHandlerException> handler) throws TwitterClientException {
        HttpGet request = new HttpGet(TwitterAPI.STREAM_STATUSES_SAMPLE_URL);

        requestStatusStream(request, handler);
    }

    public void processTrackFilterStream(final String[] keywords, final Handler<Tweet, TweetHandlerException> handler) throws TwitterClientException {
        if (keywords.length > TwitterAPI.DEFAULT_TRACK_KEYWORDS_LIMIT) {
            throw new IllegalArgumentException("the default access level allows up to "
                    + TwitterAPI.DEFAULT_TRACK_KEYWORDS_LIMIT
                    + " track keywords (you have tried to use " + keywords.length + ")");
        }

        HttpPost request = new HttpPost(TwitterAPI.STREAM_STATUSES_FILTER_URL);

        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("track", commaDelimit(keywords)));

        setEntity(request, formParams);

        requestStatusStream(request, handler);
    }

    public void processFollowFilterStream(final String[] userIds,
                                          final Handler<Tweet, TweetHandlerException> handler,
                                          final int count) throws TwitterClientException {
        if (userIds.length > TwitterAPI.DEFAULT_FOLLOW_USERIDS_LIMIT) {
            throw new IllegalArgumentException("the default access level allows up to "
                    + TwitterAPI.DEFAULT_FOLLOW_USERIDS_LIMIT
                    + " follow userids (you have tried to use " + userIds.length + ")");
        }

        HttpPost request = new HttpPost(TwitterAPI.STREAM_STATUSES_FILTER_URL);

        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("follow", commaDelimit(userIds)));
        if (count > 0) {
            formParams.add(new BasicNameValuePair("count", "" + count));
        }

        setEntity(request, formParams);

        requestStatusStream(request, handler);
    }

    ////////////////////////////////////////////////////////////////////////////

    public void updateStatus(final Tweet tweet) throws TwitterClientException {
        HttpPost request = new HttpPost(TwitterAPI.STATUSES_UPDATE_URL + ".json");

        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair(
                TwitterAPI.STATUS, tweet.getText()));
        if (null != tweet.getInReplyToTweet() && null != tweet.getInReplyToTweet().getId()) {
            formParams.add(new BasicNameValuePair(
                    TwitterAPI.IN_REPLY_TO_STATUS_ID, tweet.getInReplyToTweet().getId()));
        }

        setEntity(request, formParams);
        makeRequest(request, false);
    }

    public User findUserInfo(final String screenName) throws TwitterClientException {
        HttpGet request = new HttpGet(TwitterAPI.USERS_SHOW_URL + ".json"
                + "?" + TwitterAPI.SCREEN_NAME + "=" + screenName);

        //List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        //formParams.add(new BasicNameValuePair(TwitterAPI.SCREEN_NAME, screenName));
        //setEntity(request, formParams);
        
        JSONObject object = requestJSONObject(request);
        try {
            return new User(object);
        } catch (JSONException e) {
            throw new TwitterClientException(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private void logRequest(final HttpUriRequest request) {
        LOGGER.fine("issuing request for:  " + request.getURI());
    }

    private void setEntity(final HttpPost request,
                           final List<NameValuePair> formParams) throws TwitterClientException {
        UrlEncodedFormEntity entity;
        try {
            entity = new UrlEncodedFormEntity(formParams, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new TwitterClientException(e);
        }
        request.setEntity(entity);
    }

    private HttpResponse makeRequest(final HttpUriRequest request,
                                     final boolean openEnded) throws TwitterClientException {
        try {
            logRequest(request);

            for (Header h : request.getHeaders("Expect")) {
                System.out.println("Expect header: " + h.getName() + ", " + h.getValue());
            }

            // HttpClient seems to get the capitalization wrong ("100-Continue"), which confuses Twitter.
            request.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, false);

            setAcceptHeader(request, new String[]{"application/json"});
            setAgent(request);

            credentials.sign(request);

            for (Header h : request.getHeaders("Authorization")) {
                System.out.println("Authorization header: " + h.getName() + ", " + h.getValue());
            }

            HttpClient client = createClient(openEnded);
            HttpResponse response = client.execute(request);

            if (null == response) {
                LOGGER.severe("null response");
                return null;
            } else {
                showResponseInfo(response);

                if (200 != response.getStatusLine().getStatusCode()) {
                    response.getEntity().writeTo(System.err);
                    return null;
                } else {
                    return response;
                }
            }
        } catch (Exception e) {
            throw new TwitterClientException(e);
        }
    }

    private JSONObject requestJSONObject(final HttpUriRequest request) throws TwitterClientException {
        try {
            HttpResponse response = makeRequest(request, false);
            if (null != response) {
                HttpEntity responseEntity = response.getEntity();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                responseEntity.writeTo(bos);
                JSONObject object = new JSONObject(bos.toString());
                bos.close();
                return object;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new TwitterClientException(e);
        }
    }

    private JSONArray requestJSONArray(final HttpUriRequest request) throws TwitterClientException {
        try {
            HttpResponse response = makeRequest(request, false);
            if (null != response) {
                HttpEntity responseEntity = response.getEntity();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                responseEntity.writeTo(bos);
                JSONArray array = new JSONArray(bos.toString());
                bos.close();
                return array;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new TwitterClientException(e);
        }
    }

    private void requestStatusArray(final HttpUriRequest request,
                                    final Handler<Tweet, Exception> handler) throws TwitterClientException {
        JSONArray array = requestJSONArray(request);
        if (null != array) {
            int length = array.length();
            for (int i = 0; i < length; i++) {
                try {
                    JSONObject obj = (JSONObject) array.get(i);
                    if (!handler.handle(new Tweet(obj))) {
                        break;
                    }
                } catch (Exception e) {
                    throw new TwitterClientException(e);
                }
            }
        }
    }

    private void requestStatusStream(final HttpUriRequest request,
                                     final Handler<Tweet, TweetHandlerException> handler) throws TwitterClientException {
        try {
            HttpResponse response = makeRequest(request, true);
            if (null != response) {
                HttpEntity responseEntity = response.getEntity();
                new StatusStreamParser(handler).parse(responseEntity.getContent());
            }
        } catch (Exception e) {
            throw new TwitterClientException(e);
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
