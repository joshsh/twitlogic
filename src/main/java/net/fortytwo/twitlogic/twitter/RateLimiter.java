package net.fortytwo.twitlogic.twitter;

import net.fortytwo.twitlogic.TwitLogic;

import java.util.Date;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Apr 3, 2010
 * Time: 6:37:10 PM
 */
public class RateLimiter {
    private static final Logger LOGGER = TwitLogic.getLogger(RateLimiter.class);

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

    public void throttleRequest() throws InterruptedException {
        long now = new Date().getTime();
        long then = timestamps[currentIndex];

        long wait = HOUR - (now - then);
        if (wait > 0) {
            LOGGER.info("rate limit of " + TwitterAPI.REST_API_REQUESTS_PER_HOUR_LIMIT
                    + " requests/hour reached. Waiting " + wait + "ms");
            Thread.sleep(wait);
            now += wait;
        }

        timestamps[currentIndex] = now;
        currentIndex = (currentIndex + 1) % timestamps.length;
    }
}
