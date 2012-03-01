package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.FOAF;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.annotations.rdf;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
@rdf(FOAF.DOCUMENT)
public interface Document extends Thing {
}
