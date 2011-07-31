package net.fortytwo.twitlogic.services.twitter.twitter4j;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * User: josh
 * Date: 3/30/11
 * Time: 4:46 PM
 */
public class Twitter4jPlay {
    public static void main(final String[] args) throws Exception {
        // The factory instance is re-useable and thread safe.
        StatusListener l = new StatusListener() {
            public void onStatus(final Status s) {
                System.out.println(s.getUser().getName() + " : " + s.getText());
            }

            public void onDeletionNotice(final StatusDeletionNotice n) {
            }

            public void onTrackLimitationNotice(final int n) {
            }

            public void onException(final Exception e) {
                e.printStackTrace();
            }
        };
        TwitterStream twitterStream = new TwitterStreamFactory(l).getInstance();
        // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
        twitterStream.sample();
    }
}
