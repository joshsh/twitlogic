package net.fortytwo.twitlogic.services.twitter;

import junit.framework.TestCase;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class TwitterAPITest extends TestCase {
    public void testParseTwitterDateString() throws Exception {
        // For some reason, parsing once failed on this date string.
        TwitterAPI.parseTwitterDateString("Sat May 08 13:21:18 +0000 2010");
    }
}
