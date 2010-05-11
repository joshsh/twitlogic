package net.fortytwo.twitlogic.twitter;

import junit.framework.TestCase;

/**
 * User: josh
 * Date: May 10, 2010
 * Time: 7:52:20 PM
 */
public class TwitterAPITest extends TestCase {
    public void testParseTwitterDateString() throws Exception {
        // For some reason, parsing once failed on this date string.
        TwitterAPI.parseTwitterDateString("Sat May 08 13:21:18 +0000 2010");
    }
}
