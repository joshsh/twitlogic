package net.fortytwo.twitlogic.syntax.afterthought.impl;

import net.fortytwo.twitlogic.model.Hashtag;
import net.fortytwo.twitlogic.model.Resource;
import net.fortytwo.twitlogic.model.Triple;
import net.fortytwo.twitlogic.model.URIReference;
import net.fortytwo.twitlogic.syntax.MatcherTestBase;
import net.fortytwo.twitlogic.syntax.afterthought.DemoAfterthoughtMatcher;
import net.fortytwo.twitlogic.vocabs.RDFS;

/**
 * User: josh
 * Date: Sep 29, 2009
 * Time: 11:05:15 PM
 */
public class RdfsSeeAlsoMatcherTest extends MatcherTestBase {
    private static final Resource
            BRANDX = new Hashtag("brandx"),
            BRANDX_URL = new URIReference("http://example.org/brandx"),
            SEEALSO = new URIReference(RDFS.SEEALSO);


    public void setUp() {
        matcher = new DemoAfterthoughtMatcher();
    }

    public void testAll() throws Exception {
        assertExpected("Just tried out #brandx (see http://example.org/brandx). It's as inferior as they say.",
                new Triple(BRANDX, SEEALSO, BRANDX_URL));
    }
}