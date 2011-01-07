package net.fortytwo.twitlogic.larkc;

import eu.larkc.plugin.Plugin;
import org.openrdf.model.URI;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class StreamingPlugin extends Plugin {
    public enum OverflowPolicy { DROP_OLDEST, DROP_MOST_RECENT }

    public StreamingPlugin(final URI name) {
        super(name);
    }
}
