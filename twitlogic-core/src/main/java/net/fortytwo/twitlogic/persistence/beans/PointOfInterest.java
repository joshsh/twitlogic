package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.DBpediaResource;
import org.openrdf.elmo.annotations.rdf;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
@rdf(DBpediaResource.POINT_OF_INTEREST)
public interface PointOfInterest extends Feature {

}
