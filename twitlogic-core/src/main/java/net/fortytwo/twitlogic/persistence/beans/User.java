package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.SIOC;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.annotations.rdf;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
@rdf(SIOC.USERACCOUNT)
public interface User extends Thing {
    @rdf(SIOC.ID)
    String getId();

    void setId(String id);

    @rdf(SIOC.ACCOUNT_OF)
    Agent getAccountOf();

    void setAccountOf(Agent accountOf);
}
