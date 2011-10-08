package net.fortytwo.twitlogic.services.twitter;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.util.CommonHttpClient;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.util.Date;
import java.util.logging.Logger;

/**
 * This class enforces a rate-limiting policy on HTTP requests to a single host (Twitter's).
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RateLimiter {
    private static final Logger LOGGER = TwitLogic.getLogger(RateLimiter.class);

    // Note: should be greater than 0.
    // For a whitelisted client, Twitter allows 20,000 requests per hour, or one request every 180ms.
    private static final long MINIMUM_WAIT = 180;

    private int remainingRequests = -1;
    private long timeOfLastRequest = 0;
    private long resetTime;
    private int limit;

    public RateLimiter() {
    }

    // Enforce a wait of at least MINIMUM_WAIT between subsequent requests,
    // and observe the number of remaining requests allowed by Twitter.
    public synchronized void throttleRequest() throws InterruptedException {
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

    public synchronized void updateRateLimitStatus(final HttpResponse response) {
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
        LOGGER.fine("rate limit: " + limit + ", remaining: " + rem + ", resets at: " + new Date(res * 1000));
    }
}
