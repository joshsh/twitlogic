package net.fortytwo.twitlogic.syntax;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 6, 2009
 * Time: 7:05:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class TokenSequence {
    private final Token[] parentSequence;
    private final int startIndex;
    private final int endIndex;

    public TokenSequence(final Token[] parentSequence,
                             final int startIndex,
                             final int endIndex) {
        this.parentSequence = parentSequence;
        this.startIndex = startIndex;
        this.endIndex = endIndex;

        if (startIndex < 0 || endIndex > parentSequence.length) {
            throw new IllegalArgumentException("sequence bound(s) out of range");
        }
    }

    public Token get(final int index) {
        if (index < 0 || index > length()) {
            throw new IllegalArgumentException("index out of range");
        }

        return parentSequence[startIndex + index];
    }

    public int length() {
        return startIndex - endIndex;
    }

    public TokenSequence subsequence(final int startIndex) {
        return subsequence(startIndex, length());
    }

    public TokenSequence subsequence(final int startIndex, final int endIndex) {
        if (startIndex < 0 || endIndex > length()) {
            throw new IllegalArgumentException("index out of range");
        }

        return new TokenSequence(parentSequence, this.startIndex + startIndex, this.startIndex + endIndex);
    }
}
