package net.fortytwo.twitlogic.vocabs;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 3:25:57 AM
 */
public interface PMLJustification extends PMLProvenance {
    public static final String NAMESPACE = "http://inferenceweb.stanford.edu/2006/06/pml-justification.owl#";

    public static final String
            HASANSWER = NAMESPACE + "hasAnswer",
            HASANTECEDENTLIST = NAMESPACE + "hasAntecedentList",
            HASCONCLUSION = NAMESPACE + "hasConclusion",
            HASINFERENCERULE = NAMESPACE + "hasInferenceRule",
            INFERENCESTEP = NAMESPACE + "InferenceStep",
            ISCONSEQUENTOF = NAMESPACE + "isConsequentOf",
            NODESET = NAMESPACE + "NodeSet",
            NODESETLIST = NAMESPACE + "NodeSetList",
            QUERY = NAMESPACE + "Query";
}
