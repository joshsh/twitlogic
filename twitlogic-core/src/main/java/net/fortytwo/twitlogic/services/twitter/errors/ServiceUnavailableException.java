package net.fortytwo.twitlogic.services.twitter.errors;

import net.fortytwo.twitlogic.services.twitter.TwitterAPIException;

/**
 * 503 Service Unavailable: The Twitter servers are up, but overloaded with requests. Try again later.18) INFO 2010-03-12T21:09:28 [11]: dumping triple store, using format RDF/XML (mimeTypes=application/rdf+xml, application/xml; ext=rdf, rdfs, owl, xml) to file: /Users/josh/projects/fortytwo/twitlogic/website/dump/twitlogic-full.rdf
 *
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class ServiceUnavailableException extends TwitterAPIException {
}