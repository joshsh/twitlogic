package net.fortytwo.twitlogic.logging;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.model.Place;
import net.fortytwo.twitlogic.model.PlaceType;
import net.fortytwo.twitlogic.model.Tweet;

import java.util.Date;
import java.util.logging.Logger;

/**
 * User: josh
 * Date: Jul 19, 2010
 * Time: 2:54:34 PM
 */
public class TweetStatistics {
    protected static final Logger LOGGER = TwitLogic.getLogger(TweetStatistics.class);

    private int tweetsReceived;
    private int tweetsPersisted;
    private int placesQueued;
    private int placesDereferenced;
    private long lastTime;

    private final int[] placesByType;

    public TweetStatistics() {
        placesByType = new int[PlaceType.values().length];
        clear();
    }

    public void tweetReceived(final Tweet tweet) {
        tweetsReceived++;
    }

    public void tweetPersisted(final Tweet tweet) {
        tweetsPersisted++;
    }

    public void placeQueued(final Place place) {
        placesQueued++;
    }

    public void placeDereferenced(final Place place) {
        placesDereferenced++;
        placesByType[PlaceType.CITY.ordinal()]++;
    }

    public void clear() {
        tweetsReceived = 0;
        tweetsPersisted = 0;
        placesQueued = 0;
        placesDereferenced = 0;

        for (int i = 0; i < placesByType.length; i++) {
            placesByType[i] = 0;
        }

        lastTime = new Date().getTime();
    }

    public void logAndClear() {
        long now = new Date().getTime();

        StringBuilder sb = new StringBuilder();
        sb.append("processed ").append(tweetsReceived).append(" tweets (")
                .append(tweetsPersisted).append(" persisted)");

        sb.append(", queued ").append(placesQueued).append(" places");

        if (placesQueued > 0 || placesDereferenced > 0) {
            sb.append("(")
                    .append(placesDereferenced).append(" successfully");
            if (0 < placesDereferenced) {
                sb.append(": ");
            }

            boolean first = true;
            for (PlaceType t : PlaceType.values()) {
                int c = placesByType[t.ordinal()];
                if (c > 0) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }

                    sb.append(c).append(" ").append(t.name());
                }
            }

            sb.append(")");
        }

        sb.append(" in ").append(now - lastTime).append("ms");

        LOGGER.info(sb.toString());

        clear();
    }
}
