package net.fortytwo.twitlogic.vocabs;

/**
 * User: josh
 * Date: Apr 25, 2010
 * Time: 8:07:31 PM
 */
public interface SWC {
    public static final String NAMESPACE = "http://data.semanticweb.org/ns/swc/ontology#";
    
    public static final String
            IS_SUBEVENT_OF = NAMESPACE + "isSubEventOf",
            IS_SUPER_EVENT_OF = NAMESPACE + "isSuperEventOf";
}
