package net.fortytwo.twitlogic.util;

import junit.framework.TestCase;
import net.fortytwo.twitlogic.model.geo.Point;
import net.fortytwo.twitlogic.model.geo.Polygon;
import org.json.JSONArray;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class PolygonTest extends TestCase {

    public void testDirectionInvariance() throws Exception {
        assertCentroid(0.5, 0.5, "[[0,0], [0,1], [1,1], [1,0]]");
        assertCentroid(0.5, 0.5, "[[0,0], [1,0], [1,1], [0,1]]");
        assertCentroid(0.5, 0.5, "[[0,1], [1,1], [1,0], [0,0]]");
        assertCentroid(0.5, 0.5, "[[0,1], [1,1], [1,0], [0,0]]");
    }

    public void testTrivialPolygon() throws Exception {
        assertCentroid(0.0, 0.0, "[[0,0], [1,1]]");        
        assertCentroid(0.0, 0.0, "[[0,0], [0,0], [0,0], [0,0]]");
    }

    private void assertCentroid(final double lon,
                                final double lat,
                                final String json) throws Exception {
        Polygon p = new Polygon(new JSONArray(json));
        Point c = p.findCentroid();
        assertEquals(lon, c.getLongitude());
        assertEquals(lat, c.getLatitude());
    }
}
