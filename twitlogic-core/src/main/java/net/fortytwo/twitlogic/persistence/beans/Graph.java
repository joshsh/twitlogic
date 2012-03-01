package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.RDFG;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.annotations.rdf;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
@rdf(RDFG.GRAPH)
public interface Graph extends Thing {
}
