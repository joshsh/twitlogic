package net.fortytwo.twitlogic.syntax;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: josh
 * Date: Apr 5, 2010
 * Time: 6:51:08 PM
 */
public class TweetSyntaxTest extends TestCase {
    public void testHashtagSyntax() {
        assertTrue(TweetSyntax.HASHTAG_PATTERN.matcher("#foo").matches());
        assertTrue(TweetSyntax.HASHTAG_PATTERN.matcher("#a").matches());
        assertTrue(TweetSyntax.HASHTAG_PATTERN.matcher("#foo-bar").matches());
        assertTrue(TweetSyntax.HASHTAG_PATTERN.matcher("#foo_bar").matches());
        assertTrue(TweetSyntax.HASHTAG_PATTERN.matcher("#foo_bar_baz").matches());
        assertTrue(TweetSyntax.HASHTAG_PATTERN.matcher("#42").matches());
        assertTrue(TweetSyntax.HASHTAG_PATTERN.matcher("#6-9").matches());

        assertFalse(TweetSyntax.HASHTAG_PATTERN.matcher("#-").matches());
        assertFalse(TweetSyntax.HASHTAG_PATTERN.matcher("#_foo").matches());
        assertFalse(TweetSyntax.HASHTAG_PATTERN.matcher("#bar_").matches());
        assertFalse(TweetSyntax.HASHTAG_PATTERN.matcher("##foo").matches());
        assertFalse(TweetSyntax.HASHTAG_PATTERN.matcher("#foo_bar-").matches());
        assertFalse(TweetSyntax.HASHTAG_PATTERN.matcher("$GOOG").matches());
        assertFalse(TweetSyntax.HASHTAG_PATTERN.matcher("").matches());
        assertFalse(TweetSyntax.HASHTAG_PATTERN.matcher("blah").matches());
    }

    public void testUsernameSyntax() throws Exception {
        // TODO
    }

    public void testURLSyntax() throws Exception {
        // TODO
    }

    public void testfindHashtags() throws Exception {
        // Match all
        assertContainsHashtags("#foo", "foo");
        assertContainsHashtags("blah #foo blah blah", "foo");
        assertContainsHashtags("#foo #bar", "foo", "bar");
        assertContainsHashtags("... #foo) and #bar", "foo", "bar");
        assertContainsHashtags("##foo #bar", "bar");
        assertContainsHashtags("(#foo, #bar and #baz)... and #quux!", "foo", "bar", "baz", "quux");

        // Match nothing
        assertContainsHashtags("foobar");
        assertContainsHashtags("#foo#bar");
        assertContainsHashtags("");
        assertContainsHashtags("asdf#foo");
        assertContainsHashtags("...)#foo");
        assertContainsHashtags("...#foo");
    }

    private void assertContainsHashtags(final String text,
                                        final String... expectedTags) {
        Set<String> expected = new HashSet<String>();
        expected.addAll(Arrays.asList(expectedTags));

        Set<String> actual = TweetSyntax.findHashtags(text);

        for (String t : expected) {
            if (!actual.contains(t)) {
                fail("expected hashtag not found: " + t);
            }
        }
        for (String t : actual) {
            if (!expected.contains(t)) {
                fail("unexpected hashtag found: " + t);
            }
        }
    }
}
