package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.Geo;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.annotations.rdf;

/**
 * User: josh
 * Date: Nov 27, 2009
 * Time: 6:39:46 PM
 */
@rdf(Geo.POINT)
public interface Point extends Thing {
    @rdf(Geo.LONG)
    double getLong();
    void setLong(double l);
    
    @rdf(Geo.LAT)
    double getLat();
    void setLat(double l);
}