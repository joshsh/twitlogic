package net.fortytwo.twitlogic;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 12, 2009
 * Time: 5:59:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class TwitLogicTest extends TestCase {
    public void testNormalizeTerm() throws Exception {
        assertEquals("foo", TwitLogic.normalizeTerm("foo"));
        assertEquals("foo", TwitLogic.normalizeTerm("  foo"));
        assertEquals("foo bar", TwitLogic.normalizeTerm(" \t foo\n\n  BAR"));
        assertEquals("foo bar quux", TwitLogic.normalizeTerm(" \t foo\n\n  BAR\nqUux  \t"));
    }

    public void testIsNormalTerm() throws Exception {
        assertTrue(TwitLogic.isNormalTerm("foo"));
        assertTrue(TwitLogic.isNormalTerm("foo bar"));
        assertTrue(TwitLogic.isNormalTerm("a b cee"));

        assertFalse(TwitLogic.isNormalTerm(""));
        assertFalse(TwitLogic.isNormalTerm(" ab"));
        assertFalse(TwitLogic.isNormalTerm("ab "));
        assertFalse(TwitLogic.isNormalTerm("Abc"));
        assertFalse(TwitLogic.isNormalTerm("foo23"));
        assertFalse(TwitLogic.isNormalTerm("foo  bar"));
        assertFalse(TwitLogic.isNormalTerm("foo\tbar"));
    }
}
