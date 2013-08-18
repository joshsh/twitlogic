package net.fortytwo.twitlogic.services.twitter.twitter4j;

import net.fortytwo.twitlogic.services.twitter.TwitterAPILimits;
import twitter4j.RateLimitStatus;

import java.util.Map;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class Twitter4jLimits extends TwitterAPILimits {
    private final Map<String, RateLimitStatus> limits;

    public Twitter4jLimits(final Map<String, RateLimitStatus> limits) {
        this.limits = limits;
    }

    public int getTimelinePageCountLimit() {
        // FIXME
        return 1000;
    }

    public int getTrackKeywordsLimit() {
        // FIXME
        return 1000;
    }

    public int getFollowUserIdsLimit() {
        // FIXME
        return 1000;
    }

    public int getRestApiRequestsPerHourLimit() {
        // FIXME
        return 1000;
    }

    public int getStatusesLimit() {
        // FIXME
        return 1000;
    }
}
