package net.fortytwo.twitlogic.vocabs;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
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
