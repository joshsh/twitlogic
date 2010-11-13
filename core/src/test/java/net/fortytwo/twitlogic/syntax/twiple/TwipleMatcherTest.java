package net.fortytwo.twitlogic.syntax.twiple;

import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.PlainLiteral;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.model.TypedLiteral;
import net.fortytwo.twitlogic.syntax.MatcherTestBase;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * User: josh
 * Date: Sep 6, 2009
 * Time: 2:31:34 PM
 */
public class TwipleMatcherTest extends MatcherTestBase {
    private static final User
            JOSHSH = new User("joshsh"),
            XIXILUO = new User("xixiluo");
    private static final Hashtag KNOWS = new Hashtag("knows");

    public void setUp() {
        matcher = new TwipleMatcher();
    }

    public void tearDown() {
    }

    public void testPartsOfSpeech() throws Exception {
        assertExpected("@joshsh #knows @xixiluo",
                new Triple(JOSHSH, KNOWS, XIXILUO));
        assertExpected("#joshsh #knows @xixiluo",
                new Triple(new Hashtag("joshsh"), KNOWS, XIXILUO));
        assertExpected("\"josh\" #knows @xixiluo");
        assertExpected("http://example.org/joshsh #knows @xixiluo");

        assertExpected("@joshsh #knows @xixiluo",
                new Triple(JOSHSH, KNOWS, XIXILUO));
        assertExpected("@joshsh @knows @xixiluo");
        assertExpected("@joshsh \"knows\" @xixiluo");
        assertExpected("@joshsh http://example.org/knows @xixiluo");
        
        assertExpected("@joshsh #knows @xixiluo",
                new Triple(JOSHSH, KNOWS, XIXILUO));
        assertExpected("@joshsh #knows #xixiluo",
                new Triple(JOSHSH, KNOWS, new Hashtag("xixiluo")));
        assertExpected("@joshsh #knows \"xixiluo\"",
                new Triple(JOSHSH, KNOWS, new PlainLiteral("xixiluo")));
        assertExpected("@joshsh #knows http://example.org/xixiluo",
                new Triple(JOSHSH, KNOWS, new TypedLiteral("http://example.org/xixiluo", XMLSchema.NAMESPACE + "anyURI")));
    }

    public void testWhitespace() throws Exception {
        assertExpected("@joshsh #knows @xixiluo",
                new Triple(JOSHSH, KNOWS, XIXILUO));
        assertExpected(" and   \t\n@joshsh\t#knows \n\n@xixiluo  (I think...)  ",
                new Triple(JOSHSH, KNOWS, XIXILUO));
    }

    public void testPlainLiterals() throws Exception {
        //...
    }

    public void testURILiterals() throws Exception {
        assertExpected("@joshsh #knows http://example.org/xixiluo",
                new Triple(JOSHSH, KNOWS, new TypedLiteral("http://example.org/xixiluo", XMLSchema.NAMESPACE + "anyURI")));
        //assertExpected("@joshsh #knows http://example.org/xixiluo.", "@joshsh #knows http://example.org/xixiluo");
    }

    public void testCruft() throws Exception {
        assertExpected("%@joshsh #knows @xixiluo",
                new Triple(JOSHSH, KNOWS, XIXILUO));
        assertExpected("@joshsh #knows @xixiluo...",
                new Triple(JOSHSH, KNOWS, XIXILUO));

// RESTORE ME
//        assertExpected("foo #twipleparser #status #readyToTest .", "#twipleparser #status #readyToTest");

        assertExpected("@joshsh (#knows) @xixiluo");
    }

    public void testMultipleMatches() throws Exception {
        assertExpected("A #one, a #two, a #one #two #three #four!",
                new Triple(new Hashtag("one"), new Hashtag("two"), new Hashtag("three")),
                new Triple(new Hashtag("two"), new Hashtag("three"), new Hashtag("four")));
    }

    public void testNothingToMatch() throws Exception {
        assertExpected("There is no twiple in this tweet.");
        assertExpected("");
    }
}
