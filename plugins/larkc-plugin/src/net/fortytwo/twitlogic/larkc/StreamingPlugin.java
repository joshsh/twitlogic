package net.fortytwo.twitlogic.larkc;

import eu.larkc.plugin.Plugin;
import org.openrdf.model.URI;

/**
 * User: josh
 * Date: 1/8/11
 * Time: 12:24 AM
 */
public abstract class StreamingPlugin extends Plugin {
    public enum OverflowPolicy { DROP_OLDEST, DROP_MOST_RECENT }

    public StreamingPlugin(final URI name) {
        super(name);
    }
}
