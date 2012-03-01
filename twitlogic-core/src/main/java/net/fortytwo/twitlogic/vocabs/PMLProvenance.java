package net.fortytwo.twitlogic.vocabs;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface PMLProvenance {
    public static final String NAMESPACE = "http://inferenceweb.stanford.edu/2006/06/pml-provenance.owl#";

    public static final String
            HASCONTENT = NAMESPACE + "hasContent",
            HASRAWSTRING = NAMESPACE + "hasRawString",
            INFERENCERULE = NAMESPACE + "InferenceRule",
            INFORMATION = NAMESPACE + "Information";
}
