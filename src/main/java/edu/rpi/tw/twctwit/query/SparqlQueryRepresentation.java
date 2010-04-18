package edu.rpi.tw.twctwit.query;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

import java.io.IOException;
import java.io.OutputStream;

/**
 * User: josh
 * Date: Apr 18, 2010
 * Time: 2:51:46 PM
 */
public class SparqlQueryRepresentation extends OutputRepresentation {
    private final String query;
    private final Sail sail;
    private final int limit;

    public SparqlQueryRepresentation(final String query,
                                     final Sail sail,
                                     final int limit) {
        super(MediaType.APPLICATION_JSON);

        this.query = query;
        this.sail = sail;
        this.limit = limit;
    }

    public void write(final OutputStream out) throws IOException {
        try {
            SailConnection sc = sail.getConnection();
            try {
                try {
                    SparqlTools.queryAndWriteJSON(query, sc, out, limit);
                } catch (QueryException e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
            } finally {
                sc.close();
            }
        } catch (SailException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }
}
