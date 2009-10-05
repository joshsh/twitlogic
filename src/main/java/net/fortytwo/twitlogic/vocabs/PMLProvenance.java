package net.fortytwo.twitlogic.vocabs;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 3:36:11 AM
 */
public interface PMLProvenance {
    public static final String NAMESPACE = "http://inferenceweb.stanford.edu/2006/06/pml-provenance.owl#";

    public static final String
            HASCONTENT = NAMESPACE + "hasContent",
            HASRAWSTRING = NAMESPACE + "hasRawString",
            INFERENCERULE = NAMESPACE + "InferenceRule",
            INFORMATION = NAMESPACE + "Information";
}
