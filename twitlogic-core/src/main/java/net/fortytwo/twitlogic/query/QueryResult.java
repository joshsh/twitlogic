package net.fortytwo.twitlogic.query;

import net.fortytwo.twitlogic.proof.Query;
import org.openrdf.rio.RDFHandler;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public class QueryResult {
    private final Query answeredQuery;

    public QueryResult(final Query answeredQuery) {
        this.answeredQuery = answeredQuery;
    }

    public void handleResult(final RDFHandler handler) {

    }
}
