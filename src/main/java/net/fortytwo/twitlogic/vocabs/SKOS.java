package net.fortytwo.twitlogic.vocabs;

/**
 * User: josh
 * Date: Oct 27, 2009
 * Time: 2:34:58 AM
 */
public interface SKOS {
    public static final String NAMESPACE = "http://www.w3.org/2004/02/skos/core#";

    public static final String
            BROADER = NAMESPACE + "broader",
            NARROWER = NAMESPACE + "narrower",
            RELATED = NAMESPACE + "related";
}
