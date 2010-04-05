package net.fortytwo.twitlogic.vocabs;

/**
 * User: josh
 * Date: Oct 3, 2009
 * Time: 1:37:48 AM
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
