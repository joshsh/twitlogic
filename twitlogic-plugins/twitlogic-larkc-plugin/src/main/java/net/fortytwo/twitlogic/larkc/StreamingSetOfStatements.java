package net.fortytwo.twitlogic.larkc;


import eu.larkc.core.data.SetOfStatements;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class StreamingSetOfStatements implements SetOfStatements {
    protected final StreamingPlugin.OverflowPolicy overflowPolicy;

    public StreamingSetOfStatements(final StreamingPlugin.OverflowPolicy overflowPolicy) {
        this.overflowPolicy = overflowPolicy;
    }
}
