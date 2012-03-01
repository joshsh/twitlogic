package net.fortytwo.twitlogic.vocabs;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface RDFG {
    public static final String NAMESPACE = "http://www.w3.org/2004/03/trix/rdfg-1/";

    public static final String
            GRAPH = NAMESPACE + "Graph",
            EQUIVALENTGRAPH = NAMESPACE + "equivalentGraph",
            SUBGRAPHOF = NAMESPACE + "subGraphOf";
}
