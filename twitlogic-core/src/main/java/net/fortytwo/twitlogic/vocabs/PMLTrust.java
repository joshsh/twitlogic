package net.fortytwo.twitlogic.vocabs;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface PMLTrust extends PMLProvenance {
    public static final String NAMESPACE = "http://inferenceweb.stanford.edu/2006/06/pml-trust.owl#";

    public static final String
            FLOATMETRIC = NAMESPACE + "FloatMetric",
            HASFLOATVALUE = NAMESPACE + "hasFloatValue";
}
