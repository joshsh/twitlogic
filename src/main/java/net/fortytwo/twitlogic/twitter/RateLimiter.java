package net.fortytwo.twitlogic.twitter;

import net.fortytwo.twitlogic.TwitLogic;

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

    public RateLimiter() {
        timestamps = new long[TwitterAPI.REST_API_REQUESTS_PER_HOUR_LIMIT];
        for (int i = 0; i < timestamps.length; i++) {
            timestamps[i] = 0;
        }

        currentIndex = 0;
    }

    // Enforce a wait of at least MINIMUM_WAIT between subsequent requests,
    // and make no more than the specified limit of requests per hour.
    public void throttleRequest() throws InterruptedException {
        long last = timestamps[currentIndex];
        currentIndex = (currentIndex + 1) % timestamps.length;
        long then = timestamps[currentIndex];
        long now = new Date().getTime();

        long wait = HOUR - (now - then);
        if (wait > 0) {
            LOGGER.info("rate limit of " + TwitterAPI.REST_API_REQUESTS_PER_HOUR_LIMIT
                    + " requests/hour reached. Waiting " + wait + "ms");
            Thread.sleep(wait);
            now += wait;
        } else if (now - last < MINIMUM_WAIT) {
            wait = MINIMUM_WAIT;
            LOGGER.fine("waiting " + wait + "ms before next request");
            Thread.sleep(wait);
            now += wait;
        }

        timestamps[currentIndex] = now;
    }
}
