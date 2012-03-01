package net.fortytwo.twitlogic.vocabs;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface GeoNames {
    public static final String NAMESPACE = "http://www.geonames.org/ontology#";

    public static final String
            COUNTRYCODE = NAMESPACE + "countryCode",
            FEATURE = NAMESPACE + "Feature",
            PARENTFEATURE = NAMESPACE + "parentFeature";
            // TODO: other terms
}
