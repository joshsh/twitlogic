package net.fortytwo.twitlogic.syntax.afterthought;

import net.fortytwo.twitlogic.model.Resource;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public interface AfterthoughtParserHelper {
    void handleAfterthoughtCandidate(Resource subject,
                                     String predicateValueExpression);
}
