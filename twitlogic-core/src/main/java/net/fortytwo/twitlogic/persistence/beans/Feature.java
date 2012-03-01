package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.DCTerms;
import net.fortytwo.twitlogic.vocabs.GeoNames;
import org.openrdf.elmo.annotations.rdf;

import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
@rdf(GeoNames.FEATURE)
public interface Feature extends SpatialThing {
    @rdf(GeoNames.COUNTRYCODE)
    String getCountryCode();

    void setCountryCode(String countryCode);

    @rdf(GeoNames.PARENTFEATURE)
    Set<Feature> getParentFeature();

    void setParentFeature(Set<Feature> parentFeature);

    @rdf(DCTerms.TITLE)
    String getTitle();

    void setTitle(String title);
}
