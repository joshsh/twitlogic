package net.fortytwo.twitlogic;

import net.fortytwo.twitlogic.server.query.SparqlTools;
import junit.framework.TestCase;

/**
 * User: josh
 * Date: Jun 4, 2010
 * Time: 6:30:04 PM
 */
public class SparqlToolsTest extends TestCase {
    public void testSparqlResultFormats() throws Exception {
        assertEquals(2, SparqlTools.SparqlResultFormat.values().length);
        
        assertEquals(SparqlTools.SparqlResultFormat.XML, SparqlTools.SparqlResultFormat.values()[0]);
    }
}
