package net.fortytwo.twitlogic.server;

import org.openrdf.rio.RDFFormat;
import org.restlet.resource.Variant;
import org.restlet.data.MediaType;

import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * User: josh
 * Date: Oct 3, 2009
 * Time: 2:57:31 PM
 */
public class RDFStuff {
    private static final Map<RDFFormat, MediaType> rdfFormatToMediaTypeMap;
    private static final Map<MediaType, RDFFormat> mediaTypeToRdfFormatMap;
    private static final List<Variant> rdfVariants;

    static {
        rdfFormatToMediaTypeMap = new HashMap<RDFFormat, MediaType>();

        // Note: preserves order of insertion
        mediaTypeToRdfFormatMap = new LinkedHashMap<MediaType, RDFFormat>();

        // Note: the first format registered becomes the default format.
        registerRdfFormat(RDFFormat.RDFXML);
        registerRdfFormat(RDFFormat.TURTLE);
        registerRdfFormat(RDFFormat.N3);
        registerRdfFormat(RDFFormat.NTRIPLES);
        registerRdfFormat(RDFFormat.TRIG);
        registerRdfFormat(RDFFormat.TRIX);

        rdfVariants = new LinkedList<Variant>();
        for (MediaType mediaType : mediaTypeToRdfFormatMap.keySet()) {
            rdfVariants.add(new Variant(mediaType));
        }
    }

    public static List<Variant> getRDFVariants() {
        return rdfVariants;
    }

    public static RDFFormat findRdfFormat(final MediaType mediaType) {
        return mediaTypeToRdfFormatMap.get(mediaType);
    }

    public static MediaType findMediaType(final RDFFormat format) {
        return rdfFormatToMediaTypeMap.get(format);
    }

    private static void registerRdfFormat(final RDFFormat format) {
        MediaType t;

        if (RDFFormat.RDFXML == format) {
            t = MediaType.APPLICATION_RDF_XML;
        } else {
            t = new MediaType(format.getDefaultMIMEType());
        }

        rdfFormatToMediaTypeMap.put(format, t);
        mediaTypeToRdfFormatMap.put(t, format);
    }
}
