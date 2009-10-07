package net.fortytwo.twitlogic.syntax.afterthought;

import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.User;
import net.fortytwo.twitlogic.syntax.MatcherTestBase;
import net.fortytwo.twitlogic.vocabs.FOAF;

/**
 * User: josh
 * Date: Sep 29, 2009
 * Time: 11:05:15 PM
 */
public class AfterthoughtMatcherTest extends MatcherTestBase {
    private static Resource
            JOSHSH = new User("joshsh"),
            KNOWS = new URIReference(FOAF.KNOWS),
            XIXILUO = new User("xixiluo"),
            JOSHSH_PERSON = ((User) JOSHSH).getHeldBy(),
            XIXILUO_PERSON = ((User) XIXILUO).getHeldBy();

    public void setUp() {
        matcher = new DemoAfterthoughtMatcher();
    }

    public void testAll() throws Exception {
        assertExpected("@joshsh (who knows @xixiluo)",
                new Triple(JOSHSH_PERSON, KNOWS, XIXILUO_PERSON));
    }

    public void testWhitespaceSensitivity() throws Exception {
        assertClausesEqual("@joshsh (who knows @xixiluo)", "@joshsh (  \n who   knows@xixiluo\t)");
    }

    public void testCaseSensitivity() throws Exception {
        // Relative pronouns are not case-sensitive
        assertExpected("@joshsh (Who knows @xixiluo)",
                new Triple(JOSHSH_PERSON, KNOWS, XIXILUO_PERSON));

        // Predicates are generally not case-sensitive
        assertExpected("@joshsh (who kNows @xixiluo)",
                new Triple(JOSHSH_PERSON, KNOWS, XIXILUO_PERSON));

        // Usernames are case-sensitive.
        assertExpected("@joshsh (who knows @XixiLuo)",
                new Triple(JOSHSH_PERSON, KNOWS, new User("XixiLuo").getHeldBy()));
    }

    public void noMatch() throws Exception {
        assertExpected("@joshsh ()");
        assertExpected("@joshsh (who done it?)");
        assertExpected("@joshsh (who done @it?)");
    }
}
