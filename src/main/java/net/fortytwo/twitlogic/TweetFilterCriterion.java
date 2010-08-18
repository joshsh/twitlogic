package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.flow.Filter;
import net.fortytwo.twitlogic.model.Tweet;
import net.fortytwo.twitlogic.util.properties.TypedProperties;
import net.fortytwo.twitlogic.util.properties.PropertyException;

/**
 * User: josh
 * Date: Jun 29, 2010
 * Time: 5:22:05 PM
 */
public class TweetFilterCriterion implements Filter.Criterion<Tweet> {
    private boolean allowAllTweets = false;
    private boolean allowTweetsWithAnnotations = false;
    private boolean allowTweetsWithNanostatements = false;
    private boolean allowTweetsWithTopics = false;
    private boolean allowTweetsWithLinks = false;
    private boolean allowTweetsWithLocation = false;
    private boolean allowTweetsWithPlace = false;

    public TweetFilterCriterion() {
    }

    public TweetFilterCriterion(final TypedProperties config) throws PropertyException {
        allowAllTweets = config.getBoolean(TwitLogic.ALLOW_ALL_TWEETS, true);

        allowTweetsWithAnnotations = config.getBoolean(TwitLogic.ALLOW_TWEETS_WITH_ANNOTATIONS, allowAllTweets);
        allowTweetsWithNanostatements = config.getBoolean(TwitLogic.ALLOW_TWEETS_WITH_NANOSTATEMENTS, allowAllTweets);
        allowTweetsWithTopics = config.getBoolean(TwitLogic.ALLOW_TWEETS_WITH_TOPICS, allowAllTweets);
        allowTweetsWithLinks = config.getBoolean(TwitLogic.ALLOW_TWEETS_WITH_LINKS, allowAllTweets);
        allowTweetsWithLocation = config.getBoolean(TwitLogic.ALLOW_TWEETS_WITH_LOCATION, allowAllTweets);
        allowTweetsWithPlace = config.getBoolean(TwitLogic.ALLOW_TWEETS_WITH_PLACE, allowAllTweets);
    }

    public void setAllowAllTweets(boolean allowAllTweets) {
        this.allowAllTweets = allowAllTweets;
    }

    public void setAllowTweetsWithTopics(boolean allowTweetsWithTopics) {
        this.allowTweetsWithTopics = allowTweetsWithTopics;
    }

    public void setAllowTweetsWithLinks(boolean allowTweetsWithLinks) {
        this.allowTweetsWithLinks = allowTweetsWithLinks;
    }

    public void setAllowTweetsWithNanostatements(boolean allowTweetsWithNanostatements) {
        this.allowTweetsWithNanostatements = allowTweetsWithNanostatements;
    }

    public void setAllowTweetsWithAnnotations(boolean allowTweetsWithAnnotations) {
        this.allowTweetsWithAnnotations = allowTweetsWithAnnotations;
    }

    public void setAllowTweetsWithPlace(boolean allowTweetsWithPlace) {
        this.allowTweetsWithPlace = allowTweetsWithPlace;
    }

    public void setAllowTweetsWithLocation(boolean allowTweetsWithLocation) {
        this.allowTweetsWithLocation = allowTweetsWithLocation;
    }

    public boolean allow(final Tweet tweet) {
        return allowAllTweets
                | (allowTweetsWithAnnotations && null != tweet.getTwannotations())
                | (allowTweetsWithNanostatements && 0 != tweet.getAnnotations().size())
                | (allowTweetsWithPlace && null != tweet.getPlace())
                | (allowTweetsWithTopics && 0 != tweet.getTopics().size())
                | (allowTweetsWithLinks && 0 != tweet.getLinks().size())
                | (allowTweetsWithLocation && null != tweet.getGeo());
    }
}