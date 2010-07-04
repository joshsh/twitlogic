package net.fortytwo.twitlogic.flow;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A buffered Handler which expects exactly one thread to write to it (via
 * handle) and one thread to read from it (via flush).
 * 
 * User: josh
 * Date: Jul 1, 2010
 * Time: 7:52:21 PM
 */
public class ConcurrentBuffer<T, E extends Exception> implements Handler<T, E> {
    private final Queue<T> outQueue;
    private final Handler<T, E> handler;

    public ConcurrentBuffer(final Handler<T, E> handler) {
        this.handler = handler;
        outQueue = new ConcurrentLinkedQueue<T>();
    }

    public boolean handle(final T t) throws E {
        outQueue.add(t);
        return true;
    }

    public boolean flush() throws E {
        int size = outQueue.size();

        for (int i = 0; i < size; i++) {
            if (!handler.handle(outQueue.remove())) {
                return false;
            }
        }

        return true;
    }
}
