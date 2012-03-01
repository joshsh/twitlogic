package net.fortytwo.twitlogic.vocabs;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface DBpediaResource {
    public static final String BASE_URI = "http://dbpedia.org/resource/";

    public static final String
            ADMINISTRATIVE_DIVISION = BASE_URI + "Administrative_division",
            CITY = BASE_URI + "City",
            COUNTRY = BASE_URI + "Country",
            NEIGHBORHOOD = BASE_URI + "Neighbourhood",  // Note: spelled with 'ou'
            POINT_OF_INTEREST = BASE_URI + "Point_of_interest";
}
