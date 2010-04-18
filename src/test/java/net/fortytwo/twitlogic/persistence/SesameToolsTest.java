package net.fortytwo.twitlogic.persistence;

import junit.framework.TestCase;
import org.openrdf.rio.RDFFormat;

/**
 * User: josh
 * Date: Apr 14, 2010
 * Time: 9:10:27 PM
 */
public class SesameToolsTest extends TestCase {
    public void testRDFFormatByExtension() throws Exception {
        /*for (RDFFormat f : RDFFormat.values()) {
            System.out.println(f.getDefaultFileExtension() + ": " + f);
        }*/

        assertEquals(RDFFormat.N3, SesameTools.rdfFormatByExtension("n3"));
        assertEquals(RDFFormat.NTRIPLES, SesameTools.rdfFormatByExtension("nt"));
        assertEquals(RDFFormat.NTRIPLES, SesameTools.rdfFormatByExtension("ntriples"));
        assertEquals(RDFFormat.NTRIPLES, SesameTools.rdfFormatByExtension("ntriple"));
        assertEquals(RDFFormat.RDFXML, SesameTools.rdfFormatByExtension("rdf"));
        assertEquals(RDFFormat.RDFXML, SesameTools.rdfFormatByExtension("rdfxml"));
        assertEquals(RDFFormat.TRIG, SesameTools.rdfFormatByExtension("trig"));
        assertEquals(RDFFormat.TRIX, SesameTools.rdfFormatByExtension("xml"));
        assertEquals(RDFFormat.TRIX, SesameTools.rdfFormatByExtension("trix"));
        assertEquals(RDFFormat.TURTLE, SesameTools.rdfFormatByExtension("ttl"));
        assertEquals(RDFFormat.TURTLE, SesameTools.rdfFormatByExtension("turtle"));

        // Test case insensitivity.
        assertEquals(RDFFormat.TRIG, SesameTools.rdfFormatByExtension("TriG"));
    }
}
