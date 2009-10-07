package net.fortytwo.twitlogic.persistence;

import net.fortytwo.twitlogic.TwitLogic;
import net.fortytwo.twitlogic.vocabs.SIOC;
import net.fortytwo.twitlogic.vocabs.FOAF;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * User: josh
 * Date: Oct 2, 2009
 * Time: 10:13:36 PM
 */
public class SesameTools {
    private static final Random RANDOM = new Random();

    public static final URI
            TRIX_GRAPH = new URIImpl("http://www.w3.org/2004/03/trix/rdfg-1/Graph"),
            ADMIN_GRAPH = new URIImpl(TwitLogic.GRAPHS_BASEURI + "twitlogic-metadata"),
            COVERED_INTERVAL = new URIImpl(TwitLogic.NAMESPACE + "converedInterval"),
            TIMESTAMP = new URIImpl(TwitLogic.NAMESPACE + "timeStamp"),
            START_DATE = new URIImpl(TwitLogic.NAMESPACE + "startDate"),
            END_DATE = new URIImpl(TwitLogic.NAMESPACE + "endDate"),
            INTERVAL = new URIImpl(TwitLogic.NAMESPACE + "Interval");

    // Note: the implementation of this class is assumed to be thread-safe.
    private static final DatatypeFactory DATATYPE_FACTORY;

    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private SesameTools() {
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final Date date) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return DATATYPE_FACTORY.newXMLGregorianCalendar(cal);
    }

    public static Literal createLiteral(final Date date,
                                        final ValueFactory valueFactory) {
        return valueFactory.createLiteral(SesameTools.toXMLGregorianCalendar(date));
    }

    // TODO: improve this
    public static URI createRandomResourceURI(final ValueFactory valueFactory) {
        return valueFactory.createURI(TwitLogic.RESOURCES_BASEURI + RANDOM.nextInt(Integer.MAX_VALUE));
    }

    // TODO: improve this
    public static URI createRandomGraphURI(final ValueFactory valueFactory) {
        return valueFactory.createURI(TwitLogic.GRAPHS_BASEURI + RANDOM.nextInt(Integer.MAX_VALUE));
    }

    // TODO: improve this
    public static URI createRandomPersonURI(final ValueFactory valueFactory) {
        return valueFactory.createURI(TwitLogic.PERSONS_BASEURI + RANDOM.nextInt(Integer.MAX_VALUE));
    }
}
