package net.fortytwo.twitlogic.services.twitter.errors;

import net.fortytwo.twitlogic.services.twitter.TwitterAPIException;

/**
 * 400 Bad Request: The request was invalid.  An accompanying error message will explain why. This is the status code will be returned during rate limiting.
 *
 * User: josh
 * Date: Mar 12, 2010
 * Time: 8:32:40 PM
 */
public class BadRequestException extends TwitterAPIException {
}