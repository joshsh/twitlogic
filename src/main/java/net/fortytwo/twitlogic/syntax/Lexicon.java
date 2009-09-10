package net.fortytwo.twitlogic.syntax;

import java.util.Set;
import java.util.Collection;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 10, 2009
 * Time: 2:53:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class Lexicon {
    private final Set<String> words;

    public Lexicon(final Collection<String> wordList) {
        words = new HashSet<String>();
        for (String word : wordList) {
            if (0 < word.length()) {
                words.add(word);
            }
        }
    }

    // TODO: a basic Unix word list may have to be extended to include plurals, verb tenses, etc.
    public Lexicon(final File wordList) throws IOException {
        words = new HashSet<String>();

        BufferedReader r = new BufferedReader(new FileReader(wordList));
        String line;

        while (null != (line = r.readLine())) {
            String word = line.trim().toLowerCase();
            if (0 < word.length()) {
                words.add(word);
            }
        }

        r.close();
    }

    public boolean isWord(final String candidate) {
        // TODO: normalization may be a waste of time
        return words.contains(candidate.trim().toLowerCase());
    }
}
