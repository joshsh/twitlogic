package net.fortytwo.twitlogic.syntax;

import junit.framework.TestCase;
import net.fortytwo.twitlogic.flow.Handler;
import net.fortytwo.twitlogic.services.twitter.HandlerException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class HashtagLexerTest extends TestCase {
    private HashtagLexer lexer;

    public void setUp() {
        Set<String> words = new HashSet<String>();
        String[] a = {"on", "one", "two", "bay", "ebay"};
        words.addAll(Arrays.asList(a));
        Lexicon lexicon = new Lexicon(words);
        lexer = new HashtagLexer(lexicon);
    }

    public void testAll() throws Exception {
        assertTokenizesTo("one", "one");

        assertTokenizesTo("blah");

        assertTokenizesTo("onetwo", "one,two");

        assertTokenizesTo("onetwoblah");
        assertTokenizesTo("blahonetwo");

        assertTokenizesTo("onebay", "one,bay", "on,ebay");
    }

    public void testTrivial() throws Exception {
        assertTokenizesTo("");
    }

    private void assertTokenizesTo(final String hashtag, final String... expectedResults) throws Exception {
        final Set<String> actualResults = new HashSet<String>();

        Handler<List<String>> resultHandler = new Handler<List<String>>() {
            public boolean isOpen() {
                return true;
            }

            public void handle(List<String> result) throws HandlerException {
                actualResults.add(commaDelimit(result));
            }
        };

        lexer.tokenize(hashtag, resultHandler);

        //assertEquals(expectedResults.length, actualResults.size());

        Set<String> expectedSet = new HashSet<String>();
        expectedSet.addAll(Arrays.asList(expectedResults));
        for (String s : actualResults) {
            assertTrue("unexpected value found: " + s, expectedSet.contains(s));
        }

        for (String s : expectedResults) {
            assertTrue("expected value not found: " + s, actualResults.contains(s));
        }
    }

    private String commaDelimit(final List<String> items) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (String s : items) {
            if (first) {
                first = false;
            }                 else {
                sb.append(",");
            }

            sb.append(s);
        }

        return sb.toString();
    }
}
