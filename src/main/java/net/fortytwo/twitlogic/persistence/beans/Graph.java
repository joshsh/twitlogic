package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.RDFG;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.annotations.rdf;

/**
 * User: josh
 * Date: Nov 20, 2009
 * Time: 6:29:36 PM
 */
@rdf(RDFG.GRAPH)
public interface Graph extends Thing {
}
