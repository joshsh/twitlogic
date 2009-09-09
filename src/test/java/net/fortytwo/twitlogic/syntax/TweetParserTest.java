package net.fortytwo.twitlogic.syntax;

import junit.framework.TestCase;

import java.util.List;

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
    private List<Triple> results;

    public void setUp() {
        parser = new TweetParser();
    }

    public void tearDown() {
    }

    public void testPartsOfSpeech() throws Exception {
        results = parser.parse("@joshsh #knows @xixiluo");
        assertEquals(1, results.size());
        Triple t = results.get(0);
        assertEquals("@joshsh #knows @xixiluo", t.toString());
        assertTrue(t.getSubject() instanceof TwitterUser);
        assertTrue(t.getPredicate() instanceof Hashtag);
        assertTrue(t.getObject() instanceof TwitterUser);
    }

    public void testWrongTypeForPartOfSpeech() throws Exception {
        assertEquals(0, parser.parse("@joshsh @knows @xixiluo").size());    
    }

    public void testWhitespace() throws Exception {
        results = parser.parse("@joshsh #knows @xixiluo");

        showTwiples(parser.parse("@joshsh #knows @xixiluo"));

        showTwiples(parser.parse("foo #twipleparser #status #readyToTest ."));
    }

    public void testCruft() throws Exception {
//        assertEquals(1, parser.parse("@joshsh #knows @xixiluo.").size());
    }

    public void testNothingToMatch() throws Exception {
        assertEquals(0, parser.parse("There is no twiple in this tweet.").size());            
    }

    private void showTwiples(final List<Triple> twiples) {
        for (Triple t : twiples) {
            System.out.println("" + t);
        }
    }
}
