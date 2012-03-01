package net.fortytwo.twitlogic.vocabs;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface Review {
    public static final String NAMESPACE = "http://purl.org/stuff/rev#";

    public static final String
            COMMENT = NAMESPACE + "Comment",
            COMMENTER = NAMESPACE + "commenter",
            FEEDBACK = NAMESPACE + "Feedback",
            HASCOMMENT = NAMESPACE + "hasComment",
            HASFEEDBACK = NAMESPACE + "hasFeedback",
            HASREVIEW = NAMESPACE + "hasReview",
            MAXRATING = NAMESPACE + "maxRating",
            MINRATING = NAMESPACE + "minRating",
            POSITIVEVOTES = NAMESPACE + "positiveVotes",
            RATING = NAMESPACE + "rating",
            REVIEW = NAMESPACE + "Review",
            REVIEWER = NAMESPACE + "reviewer",
            TEXT = NAMESPACE + "text",
            TITLE = NAMESPACE + "title",
            TOTALVOTES = NAMESPACE + "totalVotes",
            TYPE = NAMESPACE + "type";
}
