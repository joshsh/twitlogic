package net.fortytwo.twitlogic.vocabs;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface SKOS {
    public static final String NAMESPACE = "http://www.w3.org/2004/02/skos/core#";

    public static final String
            BROADER = NAMESPACE + "broader",
            BROADERTRANSITIVE = NAMESPACE + "broaderTransitive",
            NARROWER = NAMESPACE + "narrower",
            NARROWERTRANSITIVE = NAMESPACE + "narrowerTransitive",
            RELATED = NAMESPACE + "related";
}
