package net.fortytwo.twitlogic.vocabs;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 3:41:58 AM
 */
public interface PMLTrust extends PMLProvenance {
    public static final String NAMESPACE = "http://inferenceweb.stanford.edu/2006/06/pml-trust.owl#";

    public static final String
            FLOATMETRIC = NAMESPACE + "FloatMetric",
            HASFLOATVALUE = NAMESPACE + "hasFloatValue";
}
