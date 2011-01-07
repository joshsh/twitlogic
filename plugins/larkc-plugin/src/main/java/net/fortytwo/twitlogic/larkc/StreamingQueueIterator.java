package net.fortytwo.twitlogic.larkc;

import eu.larkc.core.data.CloseableIterator;
import org.openrdf.model.Statement;

import java.util.Queue;

/**
 * A CloseableIterator which iterates over the elements of a Queue which is expected to grow indefinitely.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class StreamingQueueIterator<T> implements CloseableIterator<T> {
    private final Queue<T> queue;

    private Statement n;
    private boolean closed = false;
    private final SimpleCallback onClose;

    public StreamingQueueIterator(final Queue<T> queue,
                                  final SimpleCallback onClose) {
        this.queue = queue;
        this.onClose = onClose;
    }

    public boolean hasNext() {
        // A non-closed iterator over a stream always has a next element... it's just a matter of how soon one will become available.
        return !closed;
    }

    public T next() {
        return queue.remove();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Closes this iterator.  Note that this does not affect the wrapped queue,
     * except insofar as this iterator will no longer be capable of removing elements from it.
     */
    public void close() {
        if (!closed) {
            onClose.execute();
            closed = true;
        }
    }

    public boolean isClosed() {
        return closed;
    }
}
