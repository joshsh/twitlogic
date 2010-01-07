package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.SIOC;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.annotations.rdf;

/**
 * User: josh
 * Date: Nov 20, 2009
 * Time: 6:30:13 PM
 */
@rdf(SIOC.USER)
public interface User extends Thing {
    @rdf(SIOC.ID)
    String getId();

    void setId(String id);

    @rdf(SIOC.ACCOUNT_OF)
    Agent getAccountOf();

    void setAccountOf(Agent accountOf);
}
