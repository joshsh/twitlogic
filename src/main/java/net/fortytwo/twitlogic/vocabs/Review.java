package net.fortytwo.twitlogic.vocabs;

/**
 * User: josh
 * Date: Oct 3, 2009
 * Time: 1:37:48 AM
 */
public interface Review {
    public static final String NAMESPACE = "http://purl.org/stuff/rev#";

    public static final String
            COMMENT = "Comment",
            COMMENTER = "commenter",
            FEEDBACK = "Feedback",
            HASCOMMENT = "hasComment",
            HASFEEDBACK = "hasFeedback",
            HASREVIEW = "hasReview",
            MAXRATING = "maxRating",
            MINRATING = "minRating",
            POSITIVEVOTES = "positiveVotes",
            RATING = "rating",
            REVIEW = "Review",
            REVIEWER = "reviewer",
            TEXT = "text",
            TITLE = "title",
            TOTALVOTES = "totalVotes",
            TYPE = "type";
}
