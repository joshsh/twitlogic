package net.fortytwo.twitlogic.vocabs;

import net.fortytwo.twitlogic.TwitLogic;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface TwitlogicVocabulary {
    public static final String
            NAMESPACE = "http://fortytwo.net/2009/10/twitlogic#";

    public static final URI
            COVERED_INTERVAL = new URIImpl(NAMESPACE + "coveredInterval"),
            TIMESTAMP = new URIImpl(NAMESPACE + "timeStamp"),
            START_DATE = new URIImpl(NAMESPACE + "startDate"),
            END_DATE = new URIImpl(NAMESPACE + "endDate"),
            INTERVAL = new URIImpl(NAMESPACE + "Interval");

    public static final URI
            ASSOCIATION = new URIImpl(NAMESPACE + "Association"),
            SUBJECT = new URIImpl(NAMESPACE + "subject"),
            OBJECT = new URIImpl(NAMESPACE + "object"),
            WORD = new URIImpl(NAMESPACE + "Word"),
            WEIGHT = new URIImpl(NAMESPACE + "weight");

    public static final URI
            lastUpdatedAt = new URIImpl(NAMESPACE + "lastUpdatedAt");
}
