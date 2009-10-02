package net.fortytwo.twitlogic.syntax.afterthought;

import net.fortytwo.twitlogic.syntax.afterthought.impl.FoafDepictionMatcher;
import net.fortytwo.twitlogic.syntax.afterthought.impl.FoafKnowsMatcher;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 29, 2009
 * Time: 10:59:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class DemoAfterthoughtMatcher extends CompoundAfterthoughtMatcher {
    public DemoAfterthoughtMatcher() {
        super(Arrays.asList(new AfterthoughtMatcher[]{
                new FoafKnowsMatcher(),
                new FoafDepictionMatcher()
        }));
    }
}
