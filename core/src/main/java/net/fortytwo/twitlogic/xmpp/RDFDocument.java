package net.fortytwo.twitlogic.xmpp;

import org.openrdf.rio.RDFHandler;

/**
 * Created by IntelliJ IDEA.
* User: josh
* Date: Sep 23, 2009
* Time: 7:01:10 PM
* To change this template use File | Settings | File Templates.
*/
public interface RDFDocument {
    void writeTo(final RDFHandler handler);
}
