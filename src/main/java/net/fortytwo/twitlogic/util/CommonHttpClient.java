package net.fortytwo.twitlogic.util;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.twitter.TwitterClientException;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.SocketException;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Oct 7, 2009
 * Time: 12:21:38 AM
 */
public abstract class CommonHttpClient {
    protected static final Logger LOGGER = TwitLogic.getLogger(CommonHttpClient.class);

    protected static final String
            ACCEPT = "Accept",
            USER_AGENT = "User-Agent";

    private static final long
            MIN_WAIT = 10000,
            MAX_WAIT = 320000;

    protected void logRequest(final HttpUriRequest request) {
        LOGGER.fine("issuing request for:  " + request.getURI());
    }

    protected static void setAcceptHeader(final HttpRequest request, final String[] mimeTypes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mimeTypes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(mimeTypes[i]);
        }

        request.setHeader(ACCEPT, sb.toString());
    }

    protected void setAgent(final HttpRequest request) {
        request.setHeader(USER_AGENT, TwitLogic.getName() + "/" + TwitLogic.getVersion());
    }

    protected HttpClient createClient(final boolean openEnded) {
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

    protected void showResponseInfo(final HttpResponse response) {
        System.out.println("response code: " + response.getStatusLine().getStatusCode());

        HeaderIterator iter = response.headerIterator();
        while (iter.hasNext()) {
            Header h = iter.nextHeader();
            System.out.println(h.getName() + ": " + h.getValue());
        }
    }

    protected HttpResponse requestUntilSucceed(final HttpUriRequest request) throws TwitterClientException {
        long lastWait = 0;
        while (true) {
            long timeOfLastRequest = System.currentTimeMillis();
            HttpResponse response = makeRequest(request, false);
            int code = response.getStatusLine().getStatusCode();
            long wait;
            // TODO: use a different back-off policy for error responses
            if (5 == code / 100) {
                wait = nextWait(lastWait, timeOfLastRequest);
            } else {
                return response;
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

    protected HttpResponse makeRequest(final HttpUriRequest request,
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

            for (Header h : request.getHeaders("Authorization")) {
                System.out.println("Authorization header: " + h.getName() + ", " + h.getValue());
            }

            HttpClient client = createClient(openEnded);

            HttpResponse response;
            try {
                response = client.execute(request);
            } catch (SocketException e) {
                LOGGER.severe("socket error. No response to display");
                throw new TwitterClientException(e);
            }

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

    protected long nextWait(final long lastWait,
                            final long timeOfLastRequest) {
        return timeOfLastRequest + MIN_WAIT < System.currentTimeMillis()
                ? 0
                : 0 == lastWait
                ? MIN_WAIT
                : lastWait >= MAX_WAIT
                ? MAX_WAIT
                : lastWait * 2;
    }
}
