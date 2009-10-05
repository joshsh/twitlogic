package net.fortytwo.twitlogic.syntax.afterthought;

import net.fortytwo.twitlogic.syntax.afterthought.impl.FoafDepictionMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.FoafInterestMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.FoafKnowsMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.FoafMadeMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.FoafMakerMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.RdfsSeeAlsoMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.ReviewMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.SelfInterestMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.TypeMatcher;

import java.util.Arrays;

/**
 * User: josh
 * Date: Sep 29, 2009
 * Time: 10:59:40 PM
 */
public class DemoAfterthoughtMatcher extends CompoundAfterthoughtMatcher {
    public DemoAfterthoughtMatcher() {
        super(Arrays.asList(new AfterthoughtMatcher[]{
                new FoafDepictionMatcher(),
                new FoafInterestMatcher(),
                new FoafKnowsMatcher(),
                new FoafMadeMatcher(),
                new FoafMakerMatcher(),
                new RdfsSeeAlsoMatcher(),
                new ReviewMatcher(),
                new SelfInterestMatcher(),
                new TypeMatcher()
        }));
    }
}
