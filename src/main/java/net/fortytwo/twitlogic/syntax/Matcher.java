package net.fortytwo.twitlogic.syntax;

import net.fortytwo.twitlogic.Handler;
import net.fortytwo.twitlogic.TweetContext;
import net.fortytwo.twitlogic.model.Triple;

/**
 * User: josh
 * Date: Oct 1, 2009
 * Time: 8:30:00 PM
 */
public interface Matcher {
    void match(String expression,
                  Handler<Triple, MatcherException> handler,
                  TweetContext context) throws MatcherException;

    /**
 * Created by IntelliJ IDEA.
     * User: josh
     * Date: Sep 29, 2009
     * Time: 7:04:37 PM
     * To change this template use File | Settings | File Templates.
     */
    public static class TempClass {
        public static void main(final String[] args) throws Exception {
            for (String s : new String[]{"I", "we", "you", "he", "she", "it", "they", "this", "that", "who", "which"}) {
                StringBuilder sb = new StringBuilder("(");
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    sb.append("('");
                    sb.append(Character.toUpperCase(c));
                    sb.append("'|'");
                    sb.append(Character.toLowerCase(c));
                    sb.append("')");
                }
                sb.append(")");
                System.out.println(sb.toString());
            }
        }
    }
}
