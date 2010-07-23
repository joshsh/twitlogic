package net.fortytwo.twitlogic.model;

import net.fortytwo.twitlogic.persistence.beans.AdministrativeDivision;
import net.fortytwo.twitlogic.persistence.beans.City;
import net.fortytwo.twitlogic.persistence.beans.Country;
import net.fortytwo.twitlogic.persistence.beans.Neighborhood;
import net.fortytwo.twitlogic.persistence.beans.PointOfInterest;

/**
 * User: josh
 * Date: Jul 1, 2010
 * Time: 2:57:07 PM
 */
public enum PlaceType {
    ADMINISTRATIVE_DIVISION("admin", AdministrativeDivision.class),
    COUNTRY("country", Country.class),
    CITY("city", City.class),
    NEIGHBORHOOD("neighborhood", Neighborhood.class),
    POINT_OF_INTEREST("poi", PointOfInterest.class);

    private final String name;
    private Class elmoClass;
    //private final String uri;

    PlaceType(final String name,
              final Class elmoClass) {
        this.name = name;
        this.elmoClass = elmoClass;
        //this.uri = uri;
    }

    public static PlaceType lookup(final String name) {
        for (PlaceType pt : PlaceType.values()) {
            if (pt.name.equals(name)) {
                return pt;
            }
        }

        return null;
    }

    public Class getElmoClass() {
        return elmoClass;
    }

    //public String getUri() {
    //    return uri;
    //}
}
