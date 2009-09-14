package net.fortytwo.twitlogic.data;

import junit.framework.TestCase;
import net.fortytwo.twitlogic.TwitLogic;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 13, 2009
 * Time: 10:26:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class FreeAssociationGeneratorTest extends TestCase {
    public void testNormalizeTerm() throws Exception {
        assertEquals("foo", FreeAssociationGenerator.normalizeTerm("foo"));
        assertEquals("foo", FreeAssociationGenerator.normalizeTerm("  foo"));
        assertEquals("foo bar", FreeAssociationGenerator.normalizeTerm(" \t foo\n\n  BAR"));
        assertEquals("foo bar quux", FreeAssociationGenerator.normalizeTerm(" \t foo\n\n  BAR\nqUux  \t"));
    }

    public void testIsNormalTerm() throws Exception {
        assertTrue(FreeAssociationGenerator.isNormalTerm("foo"));
        assertTrue(FreeAssociationGenerator.isNormalTerm("foo bar"));
        assertTrue(FreeAssociationGenerator.isNormalTerm("a b cee"));

        assertFalse(FreeAssociationGenerator.isNormalTerm(""));
        assertFalse(FreeAssociationGenerator.isNormalTerm(" ab"));
        assertFalse(FreeAssociationGenerator.isNormalTerm("ab "));
        assertFalse(FreeAssociationGenerator.isNormalTerm("Abc"));
        assertFalse(FreeAssociationGenerator.isNormalTerm("foo23"));
        assertFalse(FreeAssociationGenerator.isNormalTerm("foo  bar"));
        assertFalse(FreeAssociationGenerator.isNormalTerm("foo\tbar"));
    }
}
