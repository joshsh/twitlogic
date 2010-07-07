package net.fortytwo.twitlogic.model;

import net.fortytwo.twitlogic.vocabs.DBpediaResource;

/**
 * User: josh
 * Date: Jul 1, 2010
 * Time: 2:57:07 PM
 */
public enum PlaceType {
    ADMINISTRATIVE_DIVISION("admin", DBpediaResource.ADMINISTRATIVE_DIVISION),
    COUNTRY("country", DBpediaResource.COUNTRY),
    CITY("city", DBpediaResource.CITY),
    NEIGHBORHOOD("neighborhood", DBpediaResource.NEIGHBORHOOD),
    POINT_OF_INTEREST("poi", DBpediaResource.POINT_OF_INTEREST);

    private final String name;
    private final String uri;

    PlaceType(final String name,
              final String uri) {
        this.name = name;
        this.uri = uri;
    }

    public static PlaceType lookup(final String name) {
        for (PlaceType pt : PlaceType.values()) {
            if (pt.name.equals(name)) {
                return pt;
            }
        }

        return null;
    }

    public String getUri() {
        return uri;
    }
}
