package net.fortytwo.twitlogic.services.twitter;

/**
 * See http://apiwiki.twitter.com/Streaming-API-Documentation#statuses/filter
 * <p/>
 * User: josh
 * Date: Aug 5, 2010
 * Time: 11:59:45 AM
 */
public abstract class TwitterAPILimits {

    abstract int getTimelinePageCountLimit();

    abstract int getTrackKeywordsLimit();

    abstract int getFollowUserIdsLimit();

    abstract int getRestApiRequestsPerHourLimit();

    /**
     * @return at most this many statuses may be retrieved from a timeline via the 'page' and 'count' REST parameters.
     */
    abstract int getStatusesLimit();

    public static final TwitterAPILimits
            DEFAULT_LIMITS,
            WHITELIST_LIMITS;

    static {
        DEFAULT_LIMITS = new TwitterAPILimits() {
            public int getTimelinePageCountLimit() {
                return 200;
            }

            public int getTrackKeywordsLimit() {
                return 200;
            }

            public int getFollowUserIdsLimit() {
                return 400;
            }

            public int getRestApiRequestsPerHourLimit() {
                return 150;
            }

            public int getStatusesLimit() {
                return 3200;
            }
        };

        // FIXME: these are not accurate
        WHITELIST_LIMITS = new TwitterAPILimits() {
            public int getTimelinePageCountLimit() {
                return 200;
            }

            public int getTrackKeywordsLimit() {
                return 200;
            }

            public int getFollowUserIdsLimit() {
                return 400;
            }

            public int getRestApiRequestsPerHourLimit() {
                return 150;
            }

            public int getStatusesLimit() {
                return 3200;
            }
        };
    }
}
