package net.fortytwo.twitlogic.server;

import org.openrdf.sail.Sail;
import org.restlet.data.Request;

/**
 * User: josh
 * Date: Oct 3, 2009
 * Time: 2:46:29 PM
 */
public interface SailSelector {
    Sail selectSail(Request request) throws Exception;
}
