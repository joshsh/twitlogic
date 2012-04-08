package net.fortytwo.twitlogic.services.twitter.errors;

import net.fortytwo.twitlogic.services.twitter.TwitterAPIException;

/**
 * 503 Service Unavailable: The Twitter servers are up, but overloaded with requests. Try again later.
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class ServiceUnavailableException extends TwitterAPIException {
}