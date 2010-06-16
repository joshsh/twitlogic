package net.fortytwo.twitlogic.services.twitter;

import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.flow.NullHandler;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.model.TweetParseException;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.services.twitter.errors.UnauthorizedException;
import net.fortytwo.twitlogic.util.CommonHttpClient;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * User: josh
 * Date: Oct 2, 2009
 * Time: 2:54:04 PM
 */
// TODO: keep track of # of requests, for reasons of rate-limiting
public class TwitterClient extends CommonHttpClient {
    private final TwitterCredentials credentials;

    // Separate clients for separate rate-limiting policies.
    private final RequestExecutor restAPIClient;
    private final RequestExecutor streamingAPIClient;
    private final RequestExecutor updateAPIClient;

    public TwitterClient() throws TwitterClientException {
        /*
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
        //*/

        credentials = new TwitterCredentials();
        credentials.loadCredentials();

        restAPIClient = new RequestExecutor() {
            private final RateLimiter rateLimiter = new RateLimiter();
            private final HttpClient client = createClient(false);

            public HttpResponse execute(HttpUriRequest request) throws TwitterClientException {
                try {
                    rateLimiter.throttleRequest();
                } catch (InterruptedException e) {
                    throw new TwitterClientException(e);
                }

                HttpResponse response;
                try {
                    response = client.execute(request);
                } catch (SocketException e) {
                    throw new TwitterConnectionResetException(e);
                } catch (IOException e) {
                    throw new TwitterClientException(e);
                }

                rateLimiter.updateRateLimitStatus(response);
                return response;
            }
        };

        // TODO: rate limiting
        streamingAPIClient = new RequestExecutor() {
            private final HttpClient client = createClient(true);

            public HttpResponse execute(HttpUriRequest request) throws TwitterClientException {
                try {
                    return client.execute(request);
                } catch (HttpHostConnectException e) {
                    LOGGER.warning("failed to connect to host: " + e);
                    throw new TwitterConnectionResetException(e);
                } catch (IOException e) {
                    throw new TwitterClientException(e);
                }
            }
        };

        // TODO: rate limiting
        updateAPIClient = new DefaultRequestExecutor();
    }

    public void requestUserTimeline(final User user,
                                    final Handler<Tweet, Exception> handler) throws TwitterClientException {
        StringBuilder sb = new StringBuilder(TwitterAPI.STATUSES_USER_TIMELINE_URL)
                .append(".json").append("?");

        if (null == user.getId()) {
            sb.append(TwitterAPI.SCREEN_NAME).append("=").append(user.getScreenName());
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
                                          final Collection<String> terms,
                                          final Handler<Tweet, TweetHandlerException> handler,
                                          final int previousStatusCount) throws TwitterClientException {
        // TODO: make this a soft error
        if (users.size() > TwitterAPI.DEFAULT_FOLLOW_USERIDS_LIMIT) {
            throw new IllegalArgumentException("the default access level allows up to "
                    + TwitterAPI.DEFAULT_FOLLOW_USERIDS_LIMIT
                    + " follow userids (you have tried to use " + users.size() + ")");
        }

        HttpPost request = new HttpPost(TwitterAPI.STREAM_STATUSES_FILTER_URL);

        List<NameValuePair> formParams = new ArrayList<NameValuePair>();

        if (0 < users.size()) {
            String followUsers = commaDelimit(userIds(users));
            LOGGER.fine("following users: " + followUsers);
            formParams.add(new BasicNameValuePair("follow", followUsers));
        }

        if (0 < terms.size()) {
            String[] ta = new String[terms.size()];
            terms.toArray(ta);
            String trackTerms = commaDelimit(ta);
            LOGGER.fine("tracking terms: " + trackTerms);
            formParams.add(new BasicNameValuePair("track", trackTerms));
        }

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
        makeSignedJSONRequest(request, updateAPIClient);
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
        } catch (TweetParseException e) {
            throw new TwitterClientException(e);
        }
    }

    public List<User> getListMembers(final User user,
                                     final String listId) throws TwitterClientException {
        List<User> users = new LinkedList<User>();

        String cursor = "-1";

        // Note: a null cursor doesn't appear to occur, but just to be safe...
        while (null != cursor && !cursor.equals("0")) {
            HttpGet request = new HttpGet(TwitterAPI.API_LISTS_URL
                    + "/" + user.getScreenName() + "/" + listId + "/members.json"
                    + "?cursor=" + cursor);
            sign(request);

            JSONObject json = requestJSONObject(request);
            //System.out.println(json);

            users.addAll(constructUserList(json));

            cursor = json.optString((TwitterAPI.UserListField.NEXT_CURSOR.toString()));
        }

        return users;
    }

    public boolean handlePublicTimelinePage(final User user,
                                            final int page,
                                            final Handler<Tweet, TweetHandlerException> handler) throws TwitterClientException, TweetHandlerException {
        if (page < 1) {
            throw new IllegalArgumentException("bad page number");
        }

        // Note: no need to authenticate
        HttpGet request = new HttpGet(TwitterAPI.USER_TIMELINE_URL
                + "/" + user.getScreenName() + ".json"
                + "?page=" + page + "&count=" + TwitterAPI.TIMELINE_PAGE_COUNT_LIMIT);

        JSONArray array = requestJSONArray(request);
        //System.out.println(array);
        for (int i = 0; i < array.length(); i++) {
            Tweet t;
            try {
                t = new Tweet(array.getJSONObject(i));
            } catch (TweetParseException e) {
                throw new TwitterClientException(e);
            } catch (JSONException e) {
                throw new TwitterClientException(e);
            }

            if (!handler.handle(t)) {
                return false;
            }
        }

        return 0 < array.length();
    }

    public void handleTimelineFrom(final User user,
                                   final Date minTimestamp,
                                   final Date maxTimestamp,
                                   final Handler<Tweet, TweetHandlerException> handler) throws TwitterClientException, TweetHandlerException {
        Handler<Tweet, TweetHandlerException> dateFilter = new Handler<Tweet, TweetHandlerException>() {
            private int statuses = 0;

            public boolean handle(final Tweet tweet) throws TweetHandlerException {
                if (++statuses >= TwitterAPI.STATUSES_LIMIT) {
                    LOGGER.warning("maximum number (" + TwitterAPI.STATUSES_LIMIT
                            + ") of statuses retrieved for user " + user.getScreenName());
                }

                Date t = tweet.getCreatedAt();

                //System.out.println("\tcreated at: " + tweet.getCreatedAt());
                return t.compareTo(maxTimestamp) > 0
                        || (t.compareTo(minTimestamp) >= 0 && handler.handle(tweet));
            }
        };

        int page = 1;
        while (handlePublicTimelinePage(user, page, dateFilter)) {
            page++;
        }
    }

    public void processTimelineFrom(final Set<User> users,
                                    final Date minTimestamp,
                                    final Date maxTimestamp,
                                    final Handler<Tweet, TweetHandlerException> handler) throws TwitterClientException, TweetHandlerException {
        for (User u : users) {
            try {
                handleTimelineFrom(u, minTimestamp, maxTimestamp, handler);
            } catch (UnauthorizedException e) { // Soft fail here
                LOGGER.warning("not authorized to get " + u.getScreenName() + "'s timeline");
            }
        }
    }

    public void processFollowers(final User user,
                                 final Handler<User, TweetHandlerException> handler) throws TwitterClientException, TweetHandlerException {
        String cursor = "-1";

        // Note: a null cursor doesn't appear to occur, but just to be safe...
        while (null != cursor && !cursor.equals("0")) {
            HttpGet request = new HttpGet(TwitterAPI.STATUSES_FRIENDS_URL
                    + ".json"
                    + (null == user.getId() ? "?screen_name=" + user.getScreenName() : "?id=" + user.getId())
                    + "&cursor=" + cursor);
            sign(request);

            JSONObject users = requestJSONObject(request);
            JSONArray array = null;
            try {
                array = users.getJSONArray("users");
            } catch (JSONException e) {
                throw new TwitterClientException(e);
            }
            //JSONArray array = requestJSONArray(request);
            //System.out.println(json);

            for (int i = 0; i < array.length(); i++) {
                User u;
                try {
                    u = new User(array.getJSONObject(i));
                } catch (TweetParseException e) {
                    throw new TwitterClientException(e);
                } catch (JSONException e) {
                    throw new TwitterClientException(e);
                }

                if (!handler.handle(u)) {
                    return;
                }
            }

            cursor = users.optString((TwitterAPI.UserListField.NEXT_CURSOR.toString()));
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private List<User> constructUserList(final JSONObject json) throws TwitterClientException {
        TwitterAPI.checkUserListJSON(json);

        List<User> users = new LinkedList<User>();
        try {
            JSONArray array = json.getJSONArray(TwitterAPI.Field.USERS.toString());
            for (int i = 0; i < array.length(); i++) {
                try {
                    users.add(new User(array.getJSONObject(i)));
                } catch (TweetParseException e) {
                    throw new TwitterClientException(e);
                }
            }
        } catch (JSONException e) {
            throw new TwitterClientException(e);
        }
        return users;
    }

    private void sign(final HttpUriRequest request) throws TwitterClientException {
        try {
            if (null != credentials) {
                credentials.sign(request);
            }
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

    private void checkForTwitterAPIException(final JSONObject json) throws TwitterAPIException {
        String msg = json.optString(TwitterAPI.ErrorField.ERROR.toString());

        if (null != msg && 0 < msg.length()) {
            System.out.println(json);
            if (msg.equals("Not authorized")) {
                throw new UnauthorizedException();
            } else {
                throw new TwitterAPIException(msg);
            }
        }
    }

    private JSONObject requestJSONObject(final HttpUriRequest request) throws TwitterClientException {
        try {
            HttpResponse response = requestUntilSucceed(request, restAPIClient);
            HttpEntity responseEntity = response.getEntity();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            responseEntity.writeTo(bos);
            JSONObject object = new JSONObject(bos.toString());
            bos.close();
            checkForTwitterAPIException(object);
            return object;
        } catch (IOException e) {
            throw new TwitterClientException(e);
        } catch (JSONException e) {
            throw new TwitterClientException(e);
        }
    }

    private JSONArray requestJSONArray(final HttpUriRequest request) throws TwitterClientException {
        try {
            HttpResponse response = requestUntilSucceed(request, restAPIClient);
            HttpEntity responseEntity = response.getEntity();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            responseEntity.writeTo(bos);
            String s = bos.toString();
            bos.close();
            // If the response starts with a '{' instead of a '[', assume it's an error
            if (s.startsWith(("{"))) {
                JSONObject object = new JSONObject(s);
                checkForTwitterAPIException(object);
            }
            return new JSONArray(s);
        } catch (IOException e) {
            throw new TwitterClientException(e);
        } catch (JSONException e) {
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
                    // TODO: should we ever be extra patient here?
                    wait = nextWait(lastWait, timeOfLastRequest, false);
                    break;
                case EXCEPTION_THROWN:
                    return exit;
                case HANDLER_QUIT:
                    return exit;
                case NULL_RESPONSE:
                    // TODO: should we ever be extra patient here?
                    wait = nextWait(lastWait, timeOfLastRequest, false);
                    break;
                case CONNECTION_REFUSED:
                    // If the connection is refused, try again, but patiently.
                    wait = CONNECTION_REFUSED_WAIT;
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
        HttpResponse response;

        try {
            response = makeSignedJSONRequest(request, streamingAPIClient);
        } catch (TwitterConnectionResetException e) {
            return StatusStreamParser.ExitReason.CONNECTION_REFUSED;
        }

        if (null != response) {
            HttpEntity responseEntity = response.getEntity();
            try {
                boolean recoverFromErrors = true;
                return new StatusStreamParser(handler, recoverFromErrors).parse(responseEntity.getContent());
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
