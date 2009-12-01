package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.FOAF;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.annotations.rdf;

/**
 * User: josh
 * Date: Nov 27, 2009
 * Time: 6:56:05 PM
 */
@rdf(FOAF.IMAGE)
public interface Image extends Thing {
}
