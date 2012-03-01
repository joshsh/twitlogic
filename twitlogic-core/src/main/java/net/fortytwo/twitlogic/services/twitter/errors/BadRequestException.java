package net.fortytwo.twitlogic.services.twitter.errors;

import net.fortytwo.twitlogic.services.twitter.TwitterAPIException;

/**
 * 400 Bad Request: The request was invalid.  An accompanying error message will explain why. This is the status code will be returned during rate limiting.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class BadRequestException extends TwitterAPIException {
}