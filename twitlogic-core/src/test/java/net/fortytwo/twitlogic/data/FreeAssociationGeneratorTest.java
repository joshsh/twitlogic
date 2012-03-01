package net.fortytwo.twitlogic.data;

import junit.framework.TestCase;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class FreeAssociationGeneratorTest extends TestCase {
    public void testNormalizeTerm() throws Exception {
        assertEquals("foo", FreeAssociationGenerator.normalizeWord("foo"));
        assertEquals("foo", FreeAssociationGenerator.normalizeWord("  foo"));
        assertEquals("foo bar", FreeAssociationGenerator.normalizeWord(" \t foo\n\n  BAR"));
        assertEquals("foo bar quux", FreeAssociationGenerator.normalizeWord(" \t foo\n\n  BAR\nqUux  \t"));
    }

    public void testIsNormalTerm() throws Exception {
        assertTrue(FreeAssociationGenerator.isNormalWord("foo"));
        assertTrue(FreeAssociationGenerator.isNormalWord("foo bar"));
        assertTrue(FreeAssociationGenerator.isNormalWord("a b cee"));

        assertFalse(FreeAssociationGenerator.isNormalWord(""));
        assertFalse(FreeAssociationGenerator.isNormalWord(" ab"));
        assertFalse(FreeAssociationGenerator.isNormalWord("ab "));
        assertFalse(FreeAssociationGenerator.isNormalWord("Abc"));
        assertFalse(FreeAssociationGenerator.isNormalWord("foo23"));
        assertFalse(FreeAssociationGenerator.isNormalWord("foo  bar"));
        assertFalse(FreeAssociationGenerator.isNormalWord("foo\tbar"));
    }
}
