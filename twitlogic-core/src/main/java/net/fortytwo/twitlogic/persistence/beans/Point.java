package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.Geo;
import org.openrdf.elmo.annotations.rdf;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
@rdf(Geo.POINT)
public interface Point extends SpatialThing {
    @rdf(Geo.LONG)
    double getLong();
    void setLong(double l);
    
    @rdf(Geo.LAT)
    double getLat();
    void setLat(double l);
}