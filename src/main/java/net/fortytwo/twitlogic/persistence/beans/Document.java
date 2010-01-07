package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.FOAF;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.annotations.rdf;

/**
 * User: josh
 * Date: Nov 27, 2009
 * Time: 6:39:46 PM
 */
@rdf(FOAF.DOCUMENT)
public interface Document extends Thing {
}
