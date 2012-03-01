package net.fortytwo.twitlogic.services.twitter.errors;

import net.fortytwo.twitlogic.services.twitter.TwitterAPIException;

/**
 * 403 Forbidden: The request is understood, but it has been refused.  An accompanying error message will explain why. This code is used when requests are being denied due to update limits.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class ForbiddenException extends TwitterAPIException {
}