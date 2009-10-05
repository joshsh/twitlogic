package net.fortytwo.twitlogic.query;

import net.fortytwo.twitlogic.proof.Query;
import org.openrdf.rio.RDFHandler;

/**
 * User: josh
 * Date: Oct 4, 2009
 * Time: 4:52:25 AM
 */
public class QueryResult {
    private final Query answeredQuery;

    public QueryResult(final Query answeredQuery) {
        this.answeredQuery = answeredQuery;
    }

    public void handleResult(final RDFHandler handler) {

    }
}
