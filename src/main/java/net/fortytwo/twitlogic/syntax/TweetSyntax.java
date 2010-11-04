package net.fortytwo.twitlogic.syntax;

import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;

/**
 * User: josh
 * Date: Apr 5, 2010
 * Time: 6:45:41 PM
 */
public class TweetSyntax {
    public static final Pattern
            DOLLARTAG_PATTERN = Pattern.compile("[$][A-Za-z]+"),
            HASHTAG_PATTERN = Pattern.compile("#[A-Za-z0-9]([A-Za-z0-9-_]*[A-Za-z0-9])*"),
            USERNAME_PATTERN = Pattern.compile("@[A-Za-z0-9-_]+"),
            URL_PATTERN = Pattern.compile("http://[A-Za-z0-9-]+([.][A-Za-z0-9-]+)*(/([A-Za-z0-9-_#&+./=?~]*[A-Za-z0-9-/])?)?");

    private static final Pattern
            LEADCHAR = Pattern.compile("[\\s\"\'\\(\\{\\{]"),
            FOLLOWCHAR = Pattern.compile("[\\s.,?!:;\"\'\\)\\}\\]]");

    public static Set<String> findDollartags(final String text) {
        Set<String> tags = new HashSet<String>();

        java.util.regex.Matcher m = DOLLARTAG_PATTERN.matcher(text);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            boolean goodLead = 0 == start
                    || LEADCHAR.matcher(text.substring(start - 1, start)).matches();
            boolean goodFollow = text.length() == end
                    || FOLLOWCHAR.matcher(text.substring(end, end + 1)).matches();
            if (goodLead && goodFollow) {
                tags.add(m.group().substring(1));
            }
        }

        return tags;
    }

    public static Set<String> findHashtags(final String text) {
        Set<String> tags = new HashSet<String>();

        java.util.regex.Matcher m = HASHTAG_PATTERN.matcher(text);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            boolean goodLead = 0 == start
                    || LEADCHAR.matcher(text.substring(start - 1, start)).matches();
            boolean goodFollow = text.length() == end
                    || FOLLOWCHAR.matcher(text.substring(end, end + 1)).matches();
            if (goodLead && goodFollow) {
                tags.add(m.group().substring(1));
            }
        }

        return tags;
    }

    public static Set<String> findLinks(final String text) {
        Set<String> links = new HashSet<String>();

        java.util.regex.Matcher m = URL_PATTERN.matcher(text);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            boolean goodLead = 0 == start
                    || LEADCHAR.matcher(text.substring(start - 1, start)).matches();
            boolean goodFollow = text.length() == end
                    || FOLLOWCHAR.matcher(text.substring(end, end + 1)).matches();
            if (goodLead && goodFollow) {
                links.add(m.group());
            }
        }

        return links;
    }
}
