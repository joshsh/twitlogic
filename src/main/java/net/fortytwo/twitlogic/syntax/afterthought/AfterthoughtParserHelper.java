package net.fortytwo.twitlogic.syntax.afterthought;

import net.fortytwo.twitlogic.model.Resource;

/**
 * User: josh
 * Date: Oct 1, 2009
 * Time: 8:27:47 PM
 */
public interface AfterthoughtParserHelper {
    void handleAfterthoughtCandidate(Resource subject,
                                     String predicateValueExpression);
}
