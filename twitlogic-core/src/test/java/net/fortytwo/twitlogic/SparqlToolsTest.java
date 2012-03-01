package net.fortytwo.twitlogic;

import net.fortytwo.sesametools.ldserver.query.SparqlTools;
import junit.framework.TestCase;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class SparqlToolsTest extends TestCase {
    public void testSparqlResultFormats() throws Exception {
        assertEquals(2, SparqlTools.SparqlResultFormat.values().length);
        
        assertEquals(SparqlTools.SparqlResultFormat.XML, SparqlTools.SparqlResultFormat.values()[0]);
    }
}
