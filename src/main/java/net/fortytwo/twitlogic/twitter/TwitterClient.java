package net.fortytwo.twitlogic.twitter;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.flow.NullHandler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.util.CommonHttpClient;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * User: josh
 * Date: Oct 2, 2009
 * Time: 2:54:04 PM
 */
// TODO: keep track of # of requests, for reasons of rate-limiting
public class TwitterClient extends CommonHttpClient {
    private final TwitterCredentials credentials;

    public TwitterClient() {
        /*
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
        //*/

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

        continuousStream(request, handler);
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

        continuousStream(request, handler);
    }


    private static String[] userIds(final Collection<User> users) {
        String[] ids = new String[users.size()];
        int i = 0;
        for (User user : users) {
            ids[i] = "" + user.getId();
            i++;
        }
        return ids;
    }

    public void processFollowFilterStream(final Collection<User> users,
                                          final Handler<Tweet, TweetHandlerException> handler,
                                          final int previousStatusCount) throws TwitterClientException {
        if (users.size() > TwitterAPI.DEFAULT_FOLLOW_USERIDS_LIMIT) {
            throw new IllegalArgumentException("the default access level allows up to "
                    + TwitterAPI.DEFAULT_FOLLOW_USERIDS_LIMIT
                    + " follow userids (you have tried to use " + users.size() + ")");
        }

        HttpPost request = new HttpPost(TwitterAPI.STREAM_STATUSES_FILTER_URL);

        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("follow", commaDelimit(userIds(users))));
        if (previousStatusCount > 0) {
            formParams.add(new BasicNameValuePair("count", "" + previousStatusCount));
        }

        setEntity(request, formParams);

        continuousStream(request, handler);
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
        sign(request);
        makeSignedJSONRequest(request, false);
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

    public List<User> getListMembers(final User user,
                                     final String listId) throws TwitterClientException {
        HttpGet request = new HttpGet(TwitterAPI.API_LISTS_URL
                + "/" + user.getScreenName() + "/" + listId + "/members.json");
        sign(request);

        JSONObject json = requestJSONObject(request);
        try {
            return constructUserList(json);
        } catch (JSONException e) {
            throw new TwitterClientException(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private List<User> constructUserList(final JSONObject json) throws JSONException {
        TwitterAPI.checkJSON(json);

        List<User> users = new LinkedList<User>();
        JSONArray array = json.getJSONArray(TwitterAPI.Field.USERS.toString());
        for (int i = 0; i < array.length(); i++) {
            users.add(new User(array.getJSONObject(i)));
        }

        return users;
    }

    private void sign(final HttpUriRequest request) throws TwitterClientException {
        try {
            credentials.sign(request);
        } catch (OAuthExpectationFailedException e) {
            throw new TwitterClientException(e);
        } catch (OAuthMessageSignerException e) {
            throw new TwitterClientException(e);
        }
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

    private JSONObject requestJSONObject(final HttpUriRequest request) throws TwitterClientException {
        try {
            HttpResponse response = requestUntilSucceed(request);
            HttpEntity responseEntity = response.getEntity();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            responseEntity.writeTo(bos);
            JSONObject object = new JSONObject(bos.toString());
            bos.close();
            return object;
        } catch (Exception e) {
            throw new TwitterClientException(e);
        }
    }

    private JSONArray requestJSONArray(final HttpUriRequest request) throws TwitterClientException {
        try {
            HttpResponse response = requestUntilSucceed(request);
            HttpEntity responseEntity = response.getEntity();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            responseEntity.writeTo(bos);
            JSONArray array = new JSONArray(bos.toString());
            bos.close();
            return array;
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

    private StatusStreamParser.ExitReason continuousStream(final HttpUriRequest request,
                                                           final Handler<Tweet, TweetHandlerException> handler) throws TwitterClientException {
        long lastWait = 0;
        while (true) {
            long timeOfLastRequest = System.currentTimeMillis();
            StatusStreamParser.ExitReason exit = singleStreamRequest(request, handler);
            long wait;
            switch (exit) {
                case END_OF_INPUT:
                    wait = nextWait(lastWait, timeOfLastRequest);
                    break;
                case EXCEPTION_THROWN:
                    return exit;
                case HANDLER_QUIT:
                    return exit;
                case NULL_RESPONSE:
                    wait = nextWait(lastWait, timeOfLastRequest);
                    break;
                default:
                    throw new IllegalStateException("unexpected exit state: " + exit);
            }

            try {
                lastWait = wait;
                LOGGER.fine("waiting " + wait + "ms before next request");
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                throw new TwitterClientException(e);
            }
        }
    }

    private StatusStreamParser.ExitReason singleStreamRequest(final HttpUriRequest request,
                                                              final Handler<Tweet, TweetHandlerException> handler) throws TwitterClientException {
        sign(request);
        HttpResponse response = makeSignedJSONRequest(request, true);
        if (null != response) {
            HttpEntity responseEntity = response.getEntity();
            try {
                return new StatusStreamParser(handler).parse(responseEntity.getContent());
            } catch (IOException e) {
                throw new TwitterClientException(e);
            } catch (TweetHandlerException e) {
                throw new TwitterClientException(e);
            }
        } else {
            return StatusStreamParser.ExitReason.NULL_RESPONSE;
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

    public static void main(final String[] args) throws Exception {
        TwitterClient client = new TwitterClient();
        client.processSampleStream(new NullHandler<Tweet, TweetHandlerException>());
    }
}
