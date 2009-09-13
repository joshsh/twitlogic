package net.fortytwo.twitlogic.syntax;

import junit.framework.TestCase;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import net.fortytwo.twitlogic.model.TwitterUser;
import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.syntax.TweetParser;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 6, 2009
 * Time: 2:31:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class TweetParserTest extends TestCase {
    private TweetParser parser;

    public void setUp() {
        parser = new TweetParser();
    }

    public void tearDown() {
    }

    public void testPartsOfSpeech() throws Exception {
        assertYields("@joshsh #knows @xixiluo", "@joshsh #knows @xixiluo");
        assertYields("#joshsh #knows @xixiluo", "#joshsh #knows @xixiluo");
        assertYields("\"josh\" #knows @xixiluo");
        assertYields("http://example.org/joshsh #knows @xixiluo");

        assertYields("@joshsh #knows @xixiluo", "@joshsh #knows @xixiluo");
        assertYields("@joshsh @knows @xixiluo");
        assertYields("@joshsh \"knows\" @xixiluo");
        assertYields("@joshsh http://example.org/knows @xixiluo");
        
        assertYields("@joshsh #knows @xixiluo", "@joshsh #knows @xixiluo");
        assertYields("@joshsh #knows #xixiluo", "@joshsh #knows #xixiluo");
        assertYields("@joshsh #knows \"xixiluo\"", "@joshsh #knows \"xixiluo\"");
        assertYields("@joshsh #knows http://example.org/xixiluo", "@joshsh #knows http://example.org/xixiluo");
    }

    public void testWhitespace() throws Exception {
        assertYields("@joshsh #knows @xixiluo", "@joshsh #knows @xixiluo");
        assertYields(" and   \t\n@joshsh\t#knows \n\n@xixiluo  (I think...)  ", "@joshsh #knows @xixiluo");
    }

    public void testPlainLiterals() throws Exception {
        //...
    }

    public void testURILiterals() throws Exception {
        assertYields("@joshsh #knows http://example.org/xixiluo", "@joshsh #knows http://example.org/xixiluo");
        //assertYields("@joshsh #knows http://example.org/xixiluo.", "@joshsh #knows http://example.org/xixiluo");
    }

    public void testCruft() throws Exception {
        assertYields("%@joshsh #knows @xixiluo", "@joshsh #knows @xixiluo");
        assertYields("@joshsh #knows @xixiluo...", "@joshsh #knows @xixiluo");
        assertYields("foo #twipleparser #status #readyToTest .", "#twipleparser #status #readyToTest");

        //assertYields("@joshsh #knows @xixiluo");
        assertYields("@joshsh (#knows) @xixiluo");
    }

    public void testMultipleMatches() throws Exception {
        assertYields("A #one, a #two, a #one #two #three #four!", "#one #two #three", "#two #three #four");
    }

    public void testNothingToMatch() throws Exception {
        assertYields("There is no twiple in this tweet.");
        assertYields("");
    }

    private void assertYields(final String text,
                              final String... exp) throws Exception {
        Set<String> expected = new HashSet<String>();
        expected.addAll(Arrays.asList(exp));

        Set<String> actual = new HashSet<String>();
        List<Triple> results = parser.parse(text);
        for (Triple t : results) {
            actual.add(t.toString());
        }

        for (String t : expected) {
            assertTrue("expected triple not found: " + t, actual.contains(t));
        }

        for (String t : actual) {
            assertTrue("unexpected triple found: " + t, expected.contains(t));
        }
    }
}
