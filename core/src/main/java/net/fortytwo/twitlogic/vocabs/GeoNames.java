package net.fortytwo.twitlogic.vocabs;

/**
 * User: josh
 * Date: Jun 15, 2010
 * Time: 3:44:45 PM
 */
public interface GeoNames {
    public static final String NAMESPACE = "http://www.geonames.org/ontology#";

    public static final String
            COUNTRYCODE = NAMESPACE + "countryCode",
            FEATURE = NAMESPACE + "Feature",
            PARENTFEATURE = NAMESPACE + "parentFeature";
            // TODO: other terms
}
