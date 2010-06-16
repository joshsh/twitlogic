package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.GeoNames;
import org.openrdf.elmo.annotations.rdf;

/**
 * User: josh
 * Date: Jun 15, 2010
 * Time: 3:44:22 PM
 */
@rdf(GeoNames.FEATURE)
public interface Feature extends SpatialThing {
    @rdf(GeoNames.COUNTRYCODE)
    String getCountryCode();

    void setCountryCode(String countryCode);
}
