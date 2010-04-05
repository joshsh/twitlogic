package net.fortytwo.twitlogic.persistence.beans;

import net.fortytwo.twitlogic.vocabs.DCTerms;
import net.fortytwo.twitlogic.vocabs.SIOC;
import net.fortytwo.twitlogic.vocabs.SIOCT;
import org.openrdf.concepts.owl.Thing;
import org.openrdf.elmo.annotations.rdf;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * User: josh
 * Date: Nov 20, 2009
 * Time: 6:29:01 PM
 */
@rdf(SIOCT.MICROBLOGPOST)
public interface MicroblogPost extends Thing {
    
    @rdf(SIOC.EMBEDS_KNOWLEDGE)
    Graph getEmbedsKnowledge();

    void setEmbedsKnowledge(Graph embedsKnowledge);

    @rdf(DCTerms.CREATED)
    XMLGregorianCalendar getCreated();

    void setCreated(XMLGregorianCalendar created);

    @rdf(SIOC.CONTENT)
    String getContent();

    void setContent(String content);

    @rdf(SIOC.HAS_CREATOR)
    User getHasCreator();

    void setHasCreator(User hasCreator);

    @rdf(SIOC.REPLY_OF)
    MicroblogPost getReplyOf();

    void setReplyOf(MicroblogPost replyOf);

    @rdf(SIOC.ADDRESSED_TO)
    User getAddressedTo();

    void setAddressedTo(User addressedTo);
}
