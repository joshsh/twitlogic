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
import net.fortytwo.twitlogic.syntax.afterthought.impl.openvocab.OVCategoryMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.openvocab.OVDepictsMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.openvocab.OVSimilarToMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.openvocab.OVStudiesMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.openvocab.OVUsesMatcher;

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
                new TypeMatcher(),
                new OVCategoryMatcher(),
                new OVDepictsMatcher(),
                new OVSimilarToMatcher(),
                new OVStudiesMatcher(),
                new OVUsesMatcher()
        }));
    }
}
