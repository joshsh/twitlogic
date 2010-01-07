package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.FOAF;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.annotations.rdf;

/**
 * User: josh
 * Date: Nov 27, 2009
 * Time: 6:26:57 PM
 */
@rdf(FOAF.SPATIALTHING)
public interface SpatialThing extends Thing {
}
