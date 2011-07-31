package net.fortytwo.twitlogic.vocabs;

/**
 * User: josh
 * Date: Nov 23, 2009
 * Time: 10:12:54 PM
 */
public interface RDFG {
    public static final String NAMESPACE = "http://www.w3.org/2004/03/trix/rdfg-1/";

    public static final String
            GRAPH = NAMESPACE + "Graph",
            EQUIVALENTGRAPH = NAMESPACE + "equivalentGraph",
            SUBGRAPHOF = NAMESPACE + "subGraphOf";
}
