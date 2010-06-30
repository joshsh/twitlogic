package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.flow.Filter;
import net.fortytwo.twitlogic.model.Tweet;

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
    private boolean allowsTweetsWithLinks = false;
    private boolean allowsTweetsWithLocation = false;
    private boolean allowsTweetsWithPlace = false;

    public void setAllowAllTweets(boolean allowAllTweets) {
        this.allowAllTweets = allowAllTweets;
    }

    public void setAllowTweetsWithTopics(boolean allowTweetsWithTopics) {
        this.allowTweetsWithTopics = allowTweetsWithTopics;
    }

    public void setAllowsTweetsWithLinks(boolean allowsTweetsWithLinks) {
        this.allowsTweetsWithLinks = allowsTweetsWithLinks;
    }

    public void setAllowTweetsWithNanostatements(boolean allowTweetsWithNanostatements) {
        this.allowTweetsWithNanostatements = allowTweetsWithNanostatements;
    }

    public void setAllowTweetsWithAnnotations(boolean allowTweetsWithAnnotations) {
        this.allowTweetsWithAnnotations = allowTweetsWithAnnotations;
    }

    public void setAllowsTweetsWithPlace(boolean allowsTweetsWithPlace) {
        this.allowsTweetsWithPlace = allowsTweetsWithPlace;
    }

    public void setAllowsTweetsWithLocation(boolean allowsTweetsWithLocation) {
        this.allowsTweetsWithLocation = allowsTweetsWithLocation;
    }

    public boolean allow(final Tweet tweet) {
        return allowAllTweets
                | (allowTweetsWithAnnotations && null != tweet.getTwannotations())
                | (allowTweetsWithNanostatements && 0 != tweet.getAnnotations().size())
                | (allowsTweetsWithPlace && null != tweet.getPlace())
                | (allowTweetsWithTopics && 0 != tweet.getTopics().size())
                | (allowsTweetsWithLinks && 0 != tweet.getLinks().size())
                | (allowsTweetsWithLocation && null != tweet.getGeo());
    }
}