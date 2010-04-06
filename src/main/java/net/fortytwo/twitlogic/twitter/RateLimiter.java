package net.fortytwo.twitlogic.twitter;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.CommonHttpClient;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.util.Date;
import java.util.logging.Logger;

/**
 * This class enforces a rate-limiting policy on HTTP requests to a single host (Twitter's).
 * <p/>
 * User: josh
 * Date: Apr 3, 2010
 * Time: 6:37:10 PM
 */
public class RateLimiter {
    private static final Logger LOGGER = TwitLogic.getLogger(RateLimiter.class);

    // Note: should be greater than 0.
    private static final long MINIMUM_WAIT = 2000;

    private static final long HOUR = 3600000;

    private final long[] timestamps;
    private int currentIndex;
    private int remainingRequests = -1;
    private long timeOfLastRequest;
    private long resetTime;
    private int limit;

    public RateLimiter() {
        timestamps = new long[TwitterAPI.DEFAULT_REST_API_REQUESTS_PER_HOUR_LIMIT];
        for (int i = 0; i < timestamps.length; i++) {
            timestamps[i] = 0;
        }

        currentIndex = 0;
    }

    // Enforce a wait of at least MINIMUM_WAIT between subsequent requests,
    // and make no more than the specified limit of requests per hour.
    public void throttleRequestOld() throws InterruptedException {
        long last = timestamps[currentIndex];
        currentIndex = (currentIndex + 1) % timestamps.length;
        long then = timestamps[currentIndex];
        long now = new Date().getTime();

        long wait = HOUR - (now - then);
        if (wait > 0) {
            LOGGER.info("rate limit of " + TwitterAPI.DEFAULT_REST_API_REQUESTS_PER_HOUR_LIMIT
                    + " requests/hour reached. Waiting " + wait + "ms");
            Thread.sleep(wait);
            now += wait;
        } else if (now - last < MINIMUM_WAIT) {
            wait = MINIMUM_WAIT;
            LOGGER.fine("waiting " + wait + "ms before issuing this request");
            Thread.sleep(wait);
            now += wait;
        }

        timestamps[currentIndex] = now;
    }

    // Enforce a wait of at least MINIMUM_WAIT between subsequent requests,
    // and observe the number of remaining requests allowed by Twitter.
    public void throttleRequest() throws InterruptedException {
        long now = new Date().getTime();

        if (0 == remainingRequests) {
            long wait = resetTime - now;
            LOGGER.info("rate limit of " + limit
                    + " requests/hour reached. Waiting " + wait + "ms until " + new Date(resetTime));
            if (0 < wait) {
                Thread.sleep(wait);
                now += wait;
            } else {
                LOGGER.warning("negative wait!");
            }
        } else if (now - timeOfLastRequest < MINIMUM_WAIT) {
            long wait = now - timeOfLastRequest;
            LOGGER.fine("waiting " + wait + "ms");
            Thread.sleep(wait);
            now += wait;
        }

        timeOfLastRequest = now;
    }

    public void updateRateLimitStatus(final HttpResponse response) {
        if (5 == response.getStatusLine().getStatusCode() / 100) {
            LOGGER.fine("did not attempt to update rate limit status, due to "
                    + response.getStatusLine().getStatusCode()
                    + " response code");
        } else {
            try {
                // Note: we assume that we actually get this response header.
                remainingRequests = Integer.valueOf(
                        response.getHeaders("X-RateLimit-Remaining")[0].getValue());
                if (0 == remainingRequests) {
                    // Reset time returned by Twitter is in seconds since the epoch.
                    resetTime = Long.valueOf(response.getHeaders("X-RateLimit-Reset")[0].getValue()) * 1000;
                    limit = Integer.valueOf(response.getHeaders("X-RateLimit-Limit")[0].getValue());
                }

                // FIXME: temporary
                showRateLimitStatus(response);
            } catch (ArrayIndexOutOfBoundsException e) {
                LOGGER.warning("could not update rate limit status. See following response info");
                CommonHttpClient.showResponseInfo(response);
            }
        }
    }

    private void showRateLimitStatus(final HttpResponse response) {
        Header[] h_lim = response.getHeaders("X-RateLimit-Limit");
        Header[] h_rem = response.getHeaders("X-RateLimit-Remaining");
        Header[] h_res = response.getHeaders("X-RateLimit-Reset");

        /*
        Header h = h_lim[0];
        String s = h.getValue();
        System.out.println("h.getName() = " + h.getName());
        System.out.println("h.getValue() = " + h.getValue());
        int i = Integer.valueOf(s);
         */

        int limit = h_lim.length == 1 ? Integer.valueOf(h_lim[0].getValue()) : -1;
        int rem = h_rem.length == 1 ? Integer.valueOf(h_rem[0].getValue()) : -1;
        long res = h_res.length == 1 ? Long.valueOf(h_res[0].getValue()) : -1;
        LOGGER.info("rate limit: " + limit + ", remaining: " + rem + ", resets at: " + new Date(res * 1000));
    }

}
