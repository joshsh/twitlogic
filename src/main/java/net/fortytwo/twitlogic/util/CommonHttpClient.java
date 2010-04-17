package net.fortytwo.twitlogic.util;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.twitter.TwitterAPIException;
import net.fortytwo.twitlogic.twitter.TwitterClient;
import net.fortytwo.twitlogic.twitter.TwitterClientException;
import net.fortytwo.twitlogic.twitter.TwitterConnectionResetException;
import net.fortytwo.twitlogic.twitter.errors.BadGatewayException;
import net.fortytwo.twitlogic.twitter.errors.BadRequestException;
import net.fortytwo.twitlogic.twitter.errors.EnhanceYourCalmException;
import net.fortytwo.twitlogic.twitter.errors.ForbiddenException;
import net.fortytwo.twitlogic.twitter.errors.InternalServerErrorException;
import net.fortytwo.twitlogic.twitter.errors.NotAcceptableException;
import net.fortytwo.twitlogic.twitter.errors.NotFoundException;
import net.fortytwo.twitlogic.twitter.errors.NotModifiedException;
import net.fortytwo.twitlogic.twitter.errors.ServiceUnavailableException;
import net.fortytwo.twitlogic.twitter.errors.UnauthorizedException;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
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

    protected static final long
            MIN_WAIT = 10000,
            MAX_WAIT = 320000,
            CONNECTION_REFUSED_WAIT = 60000;

    private static final long
            PATIENCE_FACTOR = 3;

    protected void logRequest(final HttpUriRequest request) {
        LOGGER.info("issuing HTTP " + request.getMethod() + " request for <" + request.getURI() + ">");
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

    public static void showResponseInfo(final HttpResponse response) {
        System.out.println("response code: " + response.getStatusLine().getStatusCode());

        HeaderIterator iter = response.headerIterator();
        while (iter.hasNext()) {
            Header h = iter.nextHeader();
            System.out.println(h.getName() + ": " + h.getValue());
        }
    }

    protected HttpResponse requestUntilSucceed(final HttpUriRequest request,
                                               final RequestExecutor client) throws TwitterClientException {
        long lastWait = 0;

        while (true) {
            long timeOfLastRequest = System.currentTimeMillis();

            // Wait longer if the problem may be due to Twitter being down or overloaded.
            boolean beExtraPatient = true;

            try {
                return makeSignedJSONRequest(request, client);
            } catch (NotModifiedException e) {
                throw e;
            } catch (BadRequestException e) {
                // Try again.
                // TODO: how to tell whether we're being rate limited or it is an eternally bad request?
            } catch (UnauthorizedException e) {
                LOGGER.severe("HTTP request not authorized");
                throw e;
            } catch (ForbiddenException e) {
                throw e;
            } catch (NotFoundException e) {
                throw e;
            } catch (NotAcceptableException e) {
                throw e;
            } catch (EnhanceYourCalmException e) {
                // Try again.
            } catch (InternalServerErrorException e) {
                throw e;
            } catch (BadGatewayException e) {
                // Try again.
                beExtraPatient = true;
            } catch (ServiceUnavailableException e) {
                // Try again.
                beExtraPatient = true;
            } catch (TwitterConnectionResetException e) {
                // Try again.
                beExtraPatient = true;
            }

            long wait = nextWait(lastWait, timeOfLastRequest, beExtraPatient);

            try {
                lastWait = wait;
                LOGGER.fine("waiting " + wait + "ms before next request");
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                throw new TwitterClientException(e);
            }
        }
    }

    protected HttpResponse makeSignedJSONRequest(final HttpUriRequest request,
                                                 final RequestExecutor client) throws TwitterClientException {
        logRequest(request);

        //for (Header h : request.getHeaders("Expect")) {
        //    System.out.println("Expect header: " + h.getName() + ", " + h.getValue());
        //}

        // HttpClient seems to get the capitalization wrong ("100-Continue"), which confuses Twitter.
        request.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, false);

        setAcceptHeader(request, new String[]{"application/json"});
        setAgent(request);

        //for (Header h : request.getHeaders("Authorization")) {
        //    System.out.println("Authorization header: " + h.getName() + ", " + h.getValue());
        //}

        HttpResponse response;
        response = client.execute(request);

        if (null == response) {
            LOGGER.severe("null response");
            throw new TwitterClientException("null HTTP response");
        } else {
            //showResponseInfo(response);

            int code = response.getStatusLine().getStatusCode();
            if (200 == code) {
                return response;
            } else {
                LOGGER.warning("unsuccessful request (response code " + code + ")");
                switch (code) {
                    case 304:
                        throw new NotModifiedException();
                    case 400:
                        throw new BadRequestException();
                    case 401:
                        throw new UnauthorizedException();
                    case 403:
                        throw new ForbiddenException();
                    case 404:
                        throw new NotFoundException();
                    case 406:
                        throw new NotAcceptableException();
                    case 420:
                        throw new EnhanceYourCalmException();
                    case 500:
                        throw new InternalServerErrorException();
                    case 502:
                        throw new BadGatewayException();
                    case 503:
                        throw new ServiceUnavailableException();
                    default:
                        throw new TwitterAPIException("unexpected response code: " + code);
                }
            }
        }
    }

    protected long nextWait(final long lastWait,
                            final long timeOfLastRequest,
                            final boolean beExtraPatient) {
        long minWait = beExtraPatient ? MIN_WAIT * PATIENCE_FACTOR : MIN_WAIT;
        long maxWait = beExtraPatient ? MAX_WAIT * PATIENCE_FACTOR : MAX_WAIT;

        return timeOfLastRequest + MIN_WAIT < System.currentTimeMillis()
                ? 0
                : 0 == lastWait
                ? minWait
                : lastWait >= maxWait
                ? maxWait
                : lastWait * 2;
    }

    // TODO: error handling
    // TODO: multiple (possibly circular) redirects
    public String resolve301Redirection(final String originalURI) throws TwitterClientException {
        HttpClient client = createClient(false);
        client.getParams().setBooleanParameter("http.protocol.handle-redirects", false);

        HttpUriRequest request = new HttpGet(originalURI);

        HttpResponse response;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            throw new TwitterClientException(e);
        }

        if (301 == response.getStatusLine().getStatusCode()) {
            return response.getHeaders("Location")[0].getValue();
        } else {
            return originalURI;
        }
    }

    protected interface RequestExecutor {
        HttpResponse execute(HttpUriRequest httpUriRequest) throws TwitterClientException;
    }

    public class DefaultRequestExecutor implements RequestExecutor {
        private final HttpClient client = createClient(false);

        public HttpResponse execute(HttpUriRequest request) throws TwitterClientException {
            try {
                return client.execute(request);
            } catch (IOException e) {
                throw new TwitterClientException(e);
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        CommonHttpClient client = new TwitterClient();

        String before = "http://bit.ly/1xkuDX";
        //String before = "http://twitlogic.fortytwo.net/hashtag/sdow2009";
        String after = client.resolve301Redirection(before);

        System.out.println("before: " + before);
        System.out.println("after: " + after);
    }
}
