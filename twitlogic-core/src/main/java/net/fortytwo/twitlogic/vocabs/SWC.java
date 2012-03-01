package net.fortytwo.twitlogic.vocabs;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface SWC {
    public static final String NAMESPACE = "http://data.semanticweb.org/ns/swc/ontology#";
    
    public static final String
            IS_SUBEVENT_OF = NAMESPACE + "isSubEventOf",
            IS_SUPER_EVENT_OF = NAMESPACE + "isSuperEventOf";
}
