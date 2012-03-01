package net.fortytwo.twitlogic.syntax;

import net.fortytwo.twitlogic.flow.Handler;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class HashtagLexer {
    private final Lexicon lexicon;

    public HashtagLexer(final Lexicon lexicon) {
        this.lexicon = lexicon;
    }

    public void tokenize(final String hashtag,
                         final Handler<List<String>> resultHandler) throws Exception {
        String s = hashtag.trim().toLowerCase();
        if (0 < s.length()) {
            tokenize(s, 0, resultHandler, new LinkedList<String>());
        }
    }

    private boolean tokenize(final String hashtag,
                             final int startIndex,
                             final Handler<List<String>> resultHandler,
                             final List<String> completed) throws Exception {
        if (hashtag.length() == startIndex) {
            return resultHandler.handle(completed);
        } else {
            for (int i = startIndex + 1; i <= hashtag.length(); i++) {
                String s = hashtag.substring(startIndex, i);
                if (lexicon.isWord(s)) {
                    completed.add(s);
                    if (!tokenize(hashtag, i, resultHandler, completed)) {
                        return false;
                    }
                    completed.remove(completed.size() - 1);
                }
            }
        }

        return true;
    }
}
